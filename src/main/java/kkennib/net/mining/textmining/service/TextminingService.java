// `TextminingService.java`
package kkennib.net.mining.textmining.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.enums.CleanMethod;
import kkennib.net.mining.enums.PipeLineStatus;
import kkennib.net.mining.enums.PipeTaskState;
import kkennib.net.mining.s3.service.S3Service;
import kkennib.net.mining.textmining.dto.*;
import kkennib.net.mining.textmining.entity.*;
import kkennib.net.mining.textmining.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static kkennib.net.mining.util.ResponseFactory.createErrorResponse;
import static kkennib.net.mining.util.ResponseFactory.createSuccessResponse;

import kkennib.net.mining.enums.AnalysisType;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextminingService {

  private final PipeLineRepository pipeLineRepo;
  private final PipeTaskSearchRepository pipeTaskSearchRepo;
  private final PipeTaskCleanRepository pipeTaskCleanRepo;
  private final PipeTaskFrequencyRepository pipeTaskFrequencyRepo;
  private final PipeTaskTfidfRepository pipeTaskTfidfRepo;
  private final PipeTaskConcorRepository pipeTaskConcorRepo;
  private final S3Service s3Service;


  public ServiceResponse<Map<String, List<? extends StatefulTask>>> addNewWork(
          String keyword,
          String startDateStr,
          String endDateStr,
          List<String> channelList,
          List<String> cleanList,
          List<String> analysisList,
          Long memId,
          Long projectId
  ) {
    // 날짜 변환
    OffsetDateTime startOdt = OffsetDateTime.parse(startDateStr);
    LocalDateTime startDate = startOdt.toLocalDate().atStartOfDay();
    OffsetDateTime endOdt = OffsetDateTime.parse(endDateStr);
    LocalDateTime endDate = endOdt.toLocalDate().atTime(23, 59, 59);

    // 파이프 맵 초기화
    Map<String, List<? extends StatefulTask>> pipeTaskMap = initializePipeTaskMap();

    // 파이프라인 생성
    PipeLine createdLine = createPipeLine(memId, projectId);
    if (createdLine == null) {
      return createErrorResponse("Failed to create PipeLine");
    }

    List<PipeTaskSearch> searchTasks = createSearchTasks(keyword, startDate, endDate, channelList, createdLine.getId(), memId, projectId);
    pipeTaskMap.put(PipeLineStatus.SEARCH.getValue(), searchTasks);

    List<PipeTaskClean> cleanTasks = createCleanTasks(searchTasks, createdLine.getId(), cleanList, memId, projectId);
    pipeTaskMap.put(PipeLineStatus.CLEAN.getValue(), cleanTasks);

    createAnalysisTasks(cleanTasks, createdLine.getId(), memId, projectId, analysisList);
    return createSuccessResponse(pipeTaskMap);
  }

  private Map<String, List<? extends StatefulTask>> initializePipeTaskMap() {
    Map<String, List<? extends StatefulTask>> pipeTaskMap = new HashMap<>();
    pipeTaskMap.put(PipeLineStatus.SEARCH.getValue(), new ArrayList<PipeTaskSearch>());
    pipeTaskMap.put(PipeLineStatus.CLEAN.getValue(), new ArrayList<PipeTaskClean>());
    pipeTaskMap.put(PipeLineStatus.FREQUENCY.getValue(), new ArrayList<PipeTaskFrequency>());
    pipeTaskMap.put(PipeLineStatus.TFIDF.getValue(), new ArrayList<PipeTaskTfidf>());
    pipeTaskMap.put(PipeLineStatus.CONCOR.getValue(), new ArrayList<PipeTaskConcor>());
    return pipeTaskMap;
  }

  private PipeLine createPipeLine(Long memId, Long projectId) {
    PipeLine pipeLine = new PipeLine();
    pipeLine.setCurrentStatus(PipeLineStatus.SEARCH.getValue());
    pipeLine.setMemberId(memId);
    pipeLine.setProjectId(projectId);
    PipeLine created = pipeLineRepo.save(pipeLine);
    if (created.getId() == null) {
      log.error("Failed to create PipeLine for member ID: {}", memId);
      return null;
    }
    return created;
  }

  private List<PipeTaskSearch> createSearchTasks(
          String keyword,
          LocalDateTime startDate,
          LocalDateTime endDate,
          List<String> channelList,
          Long pipeLineId,
          Long memId,
          Long projectId
  ) {
    List<PipeTaskSearch> tasks = new ArrayList<>();
    for (String channelStr : channelList) {
      PipeTaskSearch searchTask = new PipeTaskSearch();
      searchTask.setSearchKeyword(keyword);
      searchTask.setSearchStartDate(startDate);
      searchTask.setSearchEndDate(endDate);
      String[] siteChannel = channelStr.split("-");
      searchTask.setSite(siteChannel[0]);
      searchTask.setChannel(siteChannel[1]);
      searchTask.setPipeLineId(pipeLineId);
      searchTask.setMemId(memId);
      searchTask.setCurrentState(PipeTaskState.PENDING.getValue());
      searchTask.setProjectId(projectId);
      tasks.add(searchTask);
    }
    try {
      return pipeTaskSearchRepo.saveAll(tasks);
    } catch (Exception e) {
      log.error("No search tasks created for keyword: {}", keyword);
      return new ArrayList<>();
    }
  }

  private List<PipeTaskClean> createCleanTasks(
          List<PipeTaskSearch> searchTasks,
          Long pipeLineId,
          List<String> cleanList,
          Long memId,
          Long projectId
  ) {
    List<PipeTaskClean> cleanTaskList = new ArrayList<>();
    for (PipeTaskSearch searchTask : searchTasks) {
      PipeTaskClean cleanTask = new PipeTaskClean();
      cleanTask.setPipeLineId(pipeLineId);
      cleanTask.setSearchTaskId(searchTask.getId());
      cleanTask.setMemId(memId);
      cleanTask.setProjectId(projectId);

      for (String method : cleanList) {
        if (CleanMethod.EXTRACT_NOUN.getValue().equals(method)) {
          cleanTask.setExtractNoun(true);
        } else if (CleanMethod.EXTRACT_ADJECTIVE.getValue().equals(method)) {
          cleanTask.setExtractAdjective(true);
        } else if (CleanMethod.EXTRACT_VERB.getValue().equals(method)) {
          cleanTask.setExtractVerb(true);
        }
      }
      cleanTaskList.add(cleanTask);
    }
    try {
      return pipeTaskCleanRepo.saveAll(cleanTaskList);
    } catch (Exception e) {
      log.error("Failed to create clean tasks: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  private void createAnalysisTasks(
          List<PipeTaskClean> cleanTasks,
          Long pipeLineId,
          Long memId,
          Long projectId,
          List<String> analysisList
  ) {
    // 분석 타입별 클래스 매핑
    Map<String, Class<? extends AnalysisTask>> taskClassMap = Map.of(
            AnalysisType.FREQUENCY.getValue(), PipeTaskFrequency.class,
            AnalysisType.TFIDF.getValue(), PipeTaskTfidf.class,
            AnalysisType.CONCOR.getValue(), PipeTaskConcor.class
    );

    // 타입별 리스트 세팅
    Map<String, List<AnalysisTask>> taskListMap = new HashMap<>();
    for (String key : taskClassMap.keySet()) {
      taskListMap.put(key, new ArrayList<>());
    }

    // 태스크 생성
    for (PipeTaskClean cleanTask : cleanTasks) {
      Long searchTaskId = cleanTask.getSearchTaskId();
      Long cleanTaskId = cleanTask.getId();
      for (String analysisType : analysisList) {
        Class<? extends AnalysisTask> clazz = taskClassMap.get(analysisType);
        if (clazz != null) {
          addTaskList(taskListMap.get(analysisType), clazz, pipeLineId, searchTaskId, cleanTaskId, memId, projectId);
        }
      }
    }

    // 저장 및 매핑
    putPipeTaskMap(AnalysisType.FREQUENCY.getValue(), taskListMap, pipeTaskFrequencyRepo);
    putPipeTaskMap(AnalysisType.TFIDF.getValue(), taskListMap, pipeTaskTfidfRepo);
    putPipeTaskMap(AnalysisType.CONCOR.getValue(), taskListMap, pipeTaskConcorRepo);
  }

  private <T extends AnalysisTask> void putPipeTaskMap(
          String targetKey,
          Map<String, List<AnalysisTask>> taskListMap,
          JpaRepository<T, Long> repo
  ) {
    try {
      List<AnalysisTask> taskList = taskListMap.get(targetKey);
      if (taskList != null && !taskList.isEmpty()) {
        @SuppressWarnings("unchecked")
        List<T> castList = (List<T>) taskList;
        repo.saveAll(castList);
      }
    } catch (Exception e) {
      log.error("Failed to create tasks for key {}: {}", targetKey, e.getMessage());
    }
  }

  private void addTaskList(
          List<AnalysisTask> list,
          Class<? extends AnalysisTask> clazz,
          Long pipeLineId,
          Long searchTaskId,
          Long cleanTaskId,
          Long memId,
          Long projectId
  ) {
    try {
      AnalysisTask task = clazz.getDeclaredConstructor().newInstance();
      task.setPipeLineId(pipeLineId);
      task.setSearchTaskId(searchTaskId);
      task.setCleanTaskId(cleanTaskId);
      task.setMemId(memId);
      task.setProjectId(projectId);
      list.add(task);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create task instance of " + clazz.getName(), e);
    }
  }

  public <T> ServiceResponse<Boolean> deleteTasks(
          List<Long> taskIdList,
          Function<List<Long>, List<T>> findFunc,
          Consumer<List<T>> deleteFunc
  ) {
    try {
      List<T> tasks = findFunc.apply(taskIdList);
      deleteFunc.accept(tasks);
    } catch (Exception e) {
      log.error("Error deleting tasks for IDs {} - {}", taskIdList, e.getMessage());
      return createErrorResponse("Failed to delete tasks: " + taskIdList);
    }
    return createSuccessResponse(true);
  }

  @NotNull
  private <T extends StatefulTask> ServiceResponse<PipeTaskGetResponse<T>> getTasks(
          Long memId,
          Long projectId,
          BiFunction<Long, Long, List<T>> findFunc
  ) {
    List<T> taskList = new ArrayList<>();
    try {
      taskList = findFunc.apply(memId, projectId);
    } catch (Exception e) {
      log.warn("No tasks found for member ID {} or project ID: {}", memId, projectId);
    }

    Map<String, Integer> stateCountMap = new HashMap<>();
    stateCountMap.put("total", taskList.size());
    for (PipeTaskState state : PipeTaskState.values()) {
      stateCountMap.put(state.getValue(), 0);
    }
    for (T task : taskList) {
      String currentState = task.getCurrentState();
      stateCountMap.put(currentState, stateCountMap.getOrDefault(currentState, 0) + 1);
    }

    PipeTaskGetResponse<T> response = new PipeTaskGetResponse<>();
    response.setList(taskList);
    response.setCount(stateCountMap);
    return createSuccessResponse(response);
  }

  public ServiceResponse<PipeTaskDashboardResponse> getDashboardTasks(Long memId, Long projectId) {
    try {
      PipeTaskDashboardResponse dashboardResponse = new PipeTaskDashboardResponse();
      List<ItemDashboardSummary> summaryList = dashboardResponse.getSummaryList();
      for (PipeLineStatus status : PipeLineStatus.values()) {
        ItemDashboardSummary summary = new ItemDashboardSummary();
        summary.setStatus(status);
        summaryList.add(summary);
      }

      this.updateSummary(memId, projectId, PipeLineStatus.SEARCH, summaryList);
      this.updateSummary(memId, projectId, PipeLineStatus.CLEAN, summaryList);
      this.updateSummary(memId, projectId, PipeLineStatus.FREQUENCY, summaryList);
      this.updateSummary(memId, projectId, PipeLineStatus.TFIDF, summaryList);
      this.updateSummary(memId, projectId, PipeLineStatus.CONCOR, summaryList);

      dashboardResponse.setJoinedTaskList(getJoinedTasks(memId, projectId));
      return createSuccessResponse(dashboardResponse);
    } catch (Exception e) {
      log.error("Failed to get dashboard tasks for member ID: {} and project ID: {}", memId, projectId, e);
      return createErrorResponse("Failed to retrieve dashboard tasks: " + e.getMessage());
    }
  }

  public ServiceResponse<PipeTaskGetResponse<PipeTaskSearch>> getSearchTasks(Long memId, Long projectId) {
    return getTasks(memId, projectId, pipeTaskSearchRepo::findAllByMemIdAndProjectId);
  }

  public ServiceResponse<PipeTaskGetResponse<PipeTaskCleanResponse>> getCleanTasks(Long memId, Long projectId) {

    List<PipeTaskClean> taskList = new ArrayList<>();
    try {
      taskList = pipeTaskCleanRepo.findAllByMemIdAndProjectId(memId, projectId);
    } catch (Exception e) {
      log.warn("No tasks found for member ID: {} and project ID: {}", memId, projectId);
      return createErrorResponse("No clean tasks found for member ID: " + memId + " and project ID: " + projectId);
    }

    Map<String, Integer> stateCountMap = new HashMap<>();
    stateCountMap.put("total", taskList.size());
    for (PipeTaskState state : PipeTaskState.values()) {
      stateCountMap.put(state.getValue(), 0);
    }

    List<PipeTaskCleanResponse> responseList = new ArrayList<>();
    for (PipeTaskClean task : taskList) {
      String currentState = task.getCurrentState();
      stateCountMap.put(currentState, stateCountMap.getOrDefault(currentState, 0) + 1);

      PipeTaskCleanResponse response = new PipeTaskCleanResponse();
      response.setId(task.getId());
      response.setPipeLineId(task.getPipeLineId());
      response.setCurrentState(task.getCurrentState());
      response.setStartDate(task.getStartDate());
      response.setEndDate(task.getEndDate());
      response.setCreateDate(task.getCreateDate());
      response.setS3Url(task.getS3Url());
      response.setFileSize(task.getFileSize());
      response.setMemId(task.getMemId());
      response.setExtractNoun(task.isExtractNoun());
      response.setExtractAdjective(task.isExtractAdjective());
      response.setExtractVerb(task.isExtractVerb());
      response.setSearchTaskId(task.getSearchTaskId());

      Long searchTaskId = task.getSearchTaskId();
      Optional<PipeTaskSearch> searchTaskOpt = pipeTaskSearchRepo.findById(searchTaskId);
      if (searchTaskOpt.isPresent()) {
        PipeTaskSearch searchTask = searchTaskOpt.get();
        response.setSearchKeyword(searchTask.getSearchKeyword());
        response.setSite(searchTask.getSite());
        response.setChannel(searchTask.getChannel());
        response.setSearchStartDate(searchTask.getSearchStartDate());
        response.setSearchEndDate(searchTask.getSearchEndDate());
      }
      responseList.add(response);
    }

    PipeTaskGetResponse pipeTaskGetResponse = new PipeTaskGetResponse<>();
    pipeTaskGetResponse.setList(responseList);
    pipeTaskGetResponse.setCount(stateCountMap);
    return createSuccessResponse(pipeTaskGetResponse);
  }

  public ServiceResponse<PipeTaskGetResponse<PipeTaskFrequencyResponse>> getFrequencyTasks(Long memId, Long projectId) {

    List<PipeTaskFrequency> taskList = new ArrayList<>();
    try {
      taskList = pipeTaskFrequencyRepo.findAllByMemIdAndProjectId(memId, projectId);
    } catch (Exception e) {
      log.warn("No tasks found for member ID: {} and project ID: {}", memId, projectId);
      return createErrorResponse("No frequency tasks found for member ID: " + memId + " and project ID: " + projectId);
    }

    Map<String, Integer> stateCountMap = new HashMap<>();
    stateCountMap.put("total", taskList.size());
    for (PipeTaskState state : PipeTaskState.values()) {
      stateCountMap.put(state.getValue(), 0);
    }

    List<PipeTaskFrequencyResponse> responseList = new ArrayList<>();
    for (PipeTaskFrequency task : taskList) {
      String currentState = task.getCurrentState();
      stateCountMap.put(currentState, stateCountMap.getOrDefault(currentState, 0) + 1);

      PipeTaskFrequencyResponse response = new PipeTaskFrequencyResponse();
      response.setId(task.getId());
      response.setPipeLineId(task.getPipeLineId());
      response.setCurrentState(task.getCurrentState());
      response.setStartDate(task.getStartDate());
      response.setEndDate(task.getEndDate());
      response.setCreateDate(task.getCreateDate());
      response.setS3Url(task.getS3Url());
      response.setFileSize(task.getFileSize());
      response.setMemId(task.getMemId());
      response.setSearchTaskId(task.getSearchTaskId());

      Long searchTaskId = task.getSearchTaskId();
      Optional<PipeTaskSearch> searchTaskOpt = pipeTaskSearchRepo.findById(searchTaskId);
      if (searchTaskOpt.isPresent()) {
        PipeTaskSearch searchTask = searchTaskOpt.get();
        response.setSearchKeyword(searchTask.getSearchKeyword());
        response.setSite(searchTask.getSite());
        response.setChannel(searchTask.getChannel());
        response.setSearchStartDate(searchTask.getSearchStartDate());
        response.setSearchEndDate(searchTask.getSearchEndDate());
      }
      responseList.add(response);
    }

    PipeTaskGetResponse pipeTaskGetResponse = new PipeTaskGetResponse<>();
    pipeTaskGetResponse.setList(responseList);
    pipeTaskGetResponse.setCount(stateCountMap);
    return createSuccessResponse(pipeTaskGetResponse);
  }

  public ServiceResponse<PipeTaskGetResponse<PipeTaskTfidfResponse>> getTfidfTasks(Long memId, Long projectId) {

    List<PipeTaskTfidf> taskList = new ArrayList<>();
    try {
      taskList = pipeTaskTfidfRepo.findAllByMemIdAndProjectId(memId, projectId);
    } catch (Exception e) {
      log.warn("No tasks found for member ID: {} and project ID: {}", memId, projectId);
      return createErrorResponse("No TF-IDF tasks found for member ID: " + memId + " and project ID: " + projectId);
    }

    Map<String, Integer> stateCountMap = new HashMap<>();
    stateCountMap.put("total", taskList.size());
    for (PipeTaskState state : PipeTaskState.values()) {
      stateCountMap.put(state.getValue(), 0);
    }

    List<PipeTaskTfidfResponse> responseList = new ArrayList<>();
    for (PipeTaskTfidf task : taskList) {
      String currentState = task.getCurrentState();
      stateCountMap.put(currentState, stateCountMap.getOrDefault(currentState, 0) + 1);

      PipeTaskTfidfResponse response = new PipeTaskTfidfResponse();
      response.setId(task.getId());
      response.setPipeLineId(task.getPipeLineId());
      response.setCurrentState(task.getCurrentState());
      response.setStartDate(task.getStartDate());
      response.setEndDate(task.getEndDate());
      response.setCreateDate(task.getCreateDate());
      response.setS3Url(task.getS3Url());
      response.setFileSize(task.getFileSize());
      response.setMemId(task.getMemId());
      response.setSearchTaskId(task.getSearchTaskId());

      Long searchTaskId = task.getSearchTaskId();
      Optional<PipeTaskSearch> searchTaskOpt = pipeTaskSearchRepo.findById(searchTaskId);
      if (searchTaskOpt.isPresent()) {
        PipeTaskSearch searchTask = searchTaskOpt.get();
        response.setSearchKeyword(searchTask.getSearchKeyword());
        response.setSite(searchTask.getSite());
        response.setChannel(searchTask.getChannel());
        response.setSearchStartDate(searchTask.getSearchStartDate());
        response.setSearchEndDate(searchTask.getSearchEndDate());
      }
      responseList.add(response);
    }

    PipeTaskGetResponse pipeTaskGetResponse = new PipeTaskGetResponse<>();
    pipeTaskGetResponse.setList(responseList);
    pipeTaskGetResponse.setCount(stateCountMap);
    return createSuccessResponse(pipeTaskGetResponse);
  }

  public ServiceResponse<PipeTaskGetResponse<PipeTaskConcorResponse>> getConcorTasks(Long memId, Long projectId) {

    List<PipeTaskConcor> taskList = new ArrayList<>();
    try {
      taskList = pipeTaskConcorRepo.findAllByMemIdAndProjectId(memId, projectId);
    } catch (Exception e) {
      log.warn("No tasks found for member ID: {} and project ID: {}", memId, projectId);
      return createErrorResponse("No concordance tasks found for member ID: " + memId + " and project ID: " + projectId);
    }

    Map<String, Integer> stateCountMap = new HashMap<>();
    stateCountMap.put("total", taskList.size());
    for (PipeTaskState state : PipeTaskState.values()) {
      stateCountMap.put(state.getValue(), 0);
    }

    List<PipeTaskConcorResponse> responseList = new ArrayList<>();
    for (PipeTaskConcor task : taskList) {
      String currentState = task.getCurrentState();
      stateCountMap.put(currentState, stateCountMap.getOrDefault(currentState, 0) + 1);

      PipeTaskConcorResponse response = new PipeTaskConcorResponse();
      response.setId(task.getId());
      response.setPipeLineId(task.getPipeLineId());
      response.setCurrentState(task.getCurrentState());
      response.setStartDate(task.getStartDate());
      response.setEndDate(task.getEndDate());
      response.setCreateDate(task.getCreateDate());
      response.setS3Url(task.getS3Url());
      response.setFileSize(task.getFileSize());
      response.setMemId(task.getMemId());
      response.setSearchTaskId(task.getSearchTaskId());

      Long searchTaskId = task.getSearchTaskId();
      Optional<PipeTaskSearch> searchTaskOpt = pipeTaskSearchRepo.findById(searchTaskId);
      if (searchTaskOpt.isPresent()) {
        PipeTaskSearch searchTask = searchTaskOpt.get();
        response.setSearchKeyword(searchTask.getSearchKeyword());
        response.setSite(searchTask.getSite());
        response.setChannel(searchTask.getChannel());
        response.setSearchStartDate(searchTask.getSearchStartDate());
        response.setSearchEndDate(searchTask.getSearchEndDate());
      }
      responseList.add(response);
    }

    PipeTaskGetResponse pipeTaskGetResponse = new PipeTaskGetResponse<>();
    pipeTaskGetResponse.setList(responseList);
    pipeTaskGetResponse.setCount(stateCountMap);
    return createSuccessResponse(pipeTaskGetResponse);
  }

  public ServiceResponse<Boolean> deleteCleanTasks(List<Long> taskIdList) {
    return deleteTasks(taskIdList, pipeTaskCleanRepo::findAllByIdIn, pipeTaskCleanRepo::deleteAllInBatch);
  }

  public ServiceResponse<Boolean> deleteSearchTasks(List<Long> taskIdList) {
    return deleteTasks(taskIdList, pipeTaskSearchRepo::findAllByIdIn, pipeTaskSearchRepo::deleteAllInBatch);
  }

  public ServiceResponse<Boolean> deleteFrequencyTasks(List<Long> taskIdList) {
    return deleteTasks(taskIdList, pipeTaskFrequencyRepo::findAllByIdIn, pipeTaskFrequencyRepo::deleteAllInBatch);
  }

  public ServiceResponse<Boolean> deleteTfidfTasks(List<Long> taskIdList) {
    return deleteTasks(taskIdList, pipeTaskTfidfRepo::findAllByIdIn, pipeTaskTfidfRepo::deleteAllInBatch);
  }

  public ServiceResponse<Boolean> deleteConcorTasks(List<Long> taskIdList) {
    return deleteTasks(taskIdList, pipeTaskConcorRepo::findAllByIdIn, pipeTaskConcorRepo::deleteAllInBatch);
  }

  public ServiceResponse<PipeTaskPaginationResponse<ItemClean>> getCleanDataFromS3(Long id, int page, int size) {
    try {
      List<ItemClean> allCleanData = fetchAndParseCleanDataFromS3(id);
      int totalItems = allCleanData.size();
      int totalPages = (int) Math.ceil((double) totalItems / size);

      int fromIndex = (page - 1) * size;
      int toIndex = Math.min(fromIndex + size, totalItems);

      if (fromIndex > totalItems) {
        return createErrorResponse("Invalid page number");
      }

      List<ItemClean> paginatedData = allCleanData.subList(fromIndex, toIndex);
      PipeTaskPaginationResponse<ItemClean> response = new PipeTaskPaginationResponse<>();
      response.setList(paginatedData);
      response.setItemCount(paginatedData.size());
      response.setTotalCount(totalItems);

      return createSuccessResponse(response);
    } catch (Exception e) {
      log.error("Failed to retrieve clean data from S3: {}", e.getMessage(), e);
      return createErrorResponse("Failed to retrieve clean data from S3: " + e.getMessage());
    }
  }

  public ServiceResponse<PipeTaskPaginationResponse<ItemFrequency>> getFrequencyDataFromS3(Long id, int page, int size) {
    try {
      List<ItemFrequency> frequencyData = fetchAndParseFrequencyDataFromS3(id);
      int totalItems = frequencyData.size();
      int totalPages = (int) Math.ceil((double) totalItems / size);

      int fromIndex = (page - 1) * size;
      int toIndex = Math.min(fromIndex + size, totalItems);

      if (fromIndex > totalItems) {
        return createErrorResponse("Invalid page number");
      }

      List<ItemFrequency> paginatedData = frequencyData.subList(fromIndex, toIndex);

      PipeTaskPaginationResponse<ItemFrequency> response = new PipeTaskPaginationResponse<>();
      response.setList(paginatedData);
      response.setTotalCount(totalItems);
      response.setItemCount(paginatedData.size());
      return createSuccessResponse(response);
    } catch (Exception e) {
      log.error("Failed to retrieve frequency data from S3: {}", e.getMessage(), e);
      return createErrorResponse("Failed to retrieve frequency data from S3: " + e.getMessage());
    }
  }

  public ServiceResponse<PipeTaskPaginationResponse<ItemTfidf>> getTfidfDataFromS3(Long id, int page, int size) {
    try {
      List<ItemTfidf> allData = fetchAndParseTfidfDataFromS3(id);
      int totalItems = allData.size();
      int totalPages = (int) Math.ceil((double) totalItems / size);

      int fromIndex = (page - 1) * size;
      int toIndex = Math.min(fromIndex + size, totalItems);

      if (fromIndex > totalItems) {
        return createErrorResponse("Invalid page number");
      }

      List<ItemTfidf> paginatedData = allData.subList(fromIndex, toIndex);
      PipeTaskPaginationResponse<ItemTfidf> response = new PipeTaskPaginationResponse<>();
      response.setList(paginatedData);
      response.setItemCount(paginatedData.size());
      response.setTotalCount(totalItems);

      return createSuccessResponse(response);
    } catch (Exception e) {
      log.error("Failed to retrieve clean data from S3: {}", e.getMessage(), e);
      return createErrorResponse("Failed to retrieve clean data from S3: " + e.getMessage());
    }
  }

  public ServiceResponse<PipeTaskPaginationResponse<ItemConcor>> getConcorDataFromS3(Long id, int page, int size) {
    try {
      List<ItemConcor> data = fetchAndParseConcorDataFromS3(id);
      int totalItems = data.size();
      int totalPages = (int) Math.ceil((double) totalItems / size);

      int fromIndex = (page - 1) * size;
      int toIndex = Math.min(fromIndex + size, totalItems);

      if (fromIndex > totalItems) {
        return createErrorResponse("Invalid page number");
      }

      List<ItemConcor> paginatedData = data.subList(fromIndex, toIndex);

      PipeTaskPaginationResponse<ItemConcor> response = new PipeTaskPaginationResponse<>();
      response.setList(paginatedData);
      response.setTotalCount(totalItems);
      response.setItemCount(paginatedData.size());
      return createSuccessResponse(response);
    } catch (Exception e) {
      log.error("Failed to retrieve frequency data from S3: {}", e.getMessage(), e);
      return createErrorResponse("Failed to retrieve frequency data from S3: " + e.getMessage());
    }
  }

  private List<ItemClean> fetchAndParseCleanDataFromS3(Long id) {
    // Step 1: Select the record from PipeTaskClean table by id
    PipeTaskClean cleanTask = pipeTaskCleanRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Clean task not found with id: " + id));

    // Step 2: Get the S3 URL from the record
    String s3Url = cleanTask.getS3Url();
    if (s3Url == null || s3Url.isEmpty()) {
      throw new RuntimeException("No S3 URL found for clean task with id: " + id);
    }

    // Extract S3 key from the URL
    String s3Key = extractS3KeyFromUrl(s3Url);

    try {
      // Step 3: Read the S3 file content
      byte[] fileContent = s3Service.downloadFile(s3Key);

      // Step 4: Process the JSON file
      ObjectMapper objectMapper = new ObjectMapper();
      List<ItemClean> cleanData = objectMapper.readValue(fileContent, new TypeReference<List<ItemClean>>() {
      });

      log.info("Successfully processed {} clean items from JSON file", cleanData.size());
      return cleanData;
    } catch (Exception e) {
      log.error("Failed to process JSON clean data from S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to process JSON clean data from S3", e);
    }
  }

  private List<ItemFrequency> fetchAndParseFrequencyDataFromS3(Long id) {
    // Step 1: Select the record from PipeTaskFrequency table by id
    PipeTaskFrequency frequencyTask = pipeTaskFrequencyRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Frequency task not found with id: " + id));

    // Step 2: Get the S3 URL from the record
    String s3Url = frequencyTask.getS3Url();
    if (s3Url == null || s3Url.isEmpty()) {
      throw new RuntimeException("No S3 URL found for frequency task with id: " + id);
    }

    // Extract S3 key from the URL
    String s3Key = extractS3KeyFromUrl(s3Url);

    try {
      // Step 3: Read the S3 file content
      byte[] fileContent = s3Service.downloadFile(s3Key);

      // Step 4: Process each line of the NDJSON file and create ItemFrequency objects
      List<ItemFrequency> frequencyItems = new ArrayList<>();
      ObjectMapper objectMapper = new ObjectMapper();

      try (InputStream inputStream = new ByteArrayInputStream(fileContent);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

        String line;
        while ((line = reader.readLine()) != null) {
          if (line.trim().isEmpty()) {
            continue;
          }

          try {
            // Parse each JSON line (NDJSON format) to an ItemFrequency object
            ItemFrequency item = objectMapper.readValue(line, ItemFrequency.class);
            frequencyItems.add(item);
          } catch (Exception e) {
            log.warn("Failed to parse NDJSON line: {}", line, e);
          }
        }
      }

      log.info("Successfully processed {} frequency items from NDJSON file", frequencyItems.size());
      return frequencyItems;
    } catch (Exception e) {
      log.error("Failed to process NDJSON frequency data from S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to process NDJSON frequency data from S3", e);
    }
  }

  private List<ItemTfidf> fetchAndParseTfidfDataFromS3(Long id) {
    // Step 1: Select the record from PipeTaskClean table by id
    PipeTaskTfidf task = pipeTaskTfidfRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Clean task not found with id: " + id));

    // Step 2: Get the S3 URL from the record
    String s3Url = task.getS3Url();
    if (s3Url == null || s3Url.isEmpty()) {
      throw new RuntimeException("No S3 URL found for clean task with id: " + id);
    }

    // Extract S3 key from the URL
    String s3Key = extractS3KeyFromUrl(s3Url);

    try {
      // Step 3: Read the S3 file content
      byte[] fileContent = s3Service.downloadFile(s3Key);

      // Step 4: Process the JSON file
      ObjectMapper objectMapper = new ObjectMapper();
      List<ItemTfidf> data = objectMapper.readValue(fileContent, new TypeReference<List<ItemTfidf>>() {
      });

      log.info("Successfully processed {} clean items from JSON file", data.size());
      return data;
    } catch (Exception e) {
      log.error("Failed to process JSON clean data from S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to process JSON clean data from S3", e);
    }
  }

  private List<ItemConcor> fetchAndParseConcorDataFromS3(Long id) {
    // Step 1: Select the record from PipeTaskFrequency table by id
    PipeTaskConcor frequencyTask = pipeTaskConcorRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Frequency task not found with id: " + id));

    // Step 2: Get the S3 URL from the record
    String s3Url = frequencyTask.getS3Url();
    if (s3Url == null || s3Url.isEmpty()) {
      throw new RuntimeException("No S3 URL found for frequency task with id: " + id);
    }

    // Extract S3 key from the URL
    String s3Key = extractS3KeyFromUrl(s3Url);

    try {
      // Step 3: Read the S3 file content
      byte[] fileContent = s3Service.downloadFile(s3Key);

      // Step 4: Process each line of the NDJSON file and create ItemConcor objects
      List<ItemConcor> items = new ArrayList<>();
      ObjectMapper objectMapper = new ObjectMapper();

      try (InputStream inputStream = new ByteArrayInputStream(fileContent);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

        String line;
        while ((line = reader.readLine()) != null) {
          if (line.trim().isEmpty()) {
            continue;
          }

          try {
            // Parse each JSON line (NDJSON format) to an ItemFrequency object
            ItemConcor item = objectMapper.readValue(line, ItemConcor.class);
            items.add(item);
          } catch (Exception e) {
            log.warn("Failed to parse NDJSON line: {}", line, e);
          }
        }
      }

      log.info("Successfully processed {} frequency items from NDJSON file", items.size());
      return items;
    } catch (Exception e) {
      log.error("Failed to process NDJSON frequency data from S3: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to process NDJSON frequency data from S3", e);
    }
  }

  private String extractS3KeyFromUrl(String s3Url) {
    // Example S3 URL: https://bucket-name.s3.region.amazonaws.com/path/to/file.txt
    // We need to extract the key part: path/to/file.txt

    // If URL doesn't contain expected S3 format, return the URL as is
    if (!s3Url.contains("amazonaws.com/")) {
      return s3Url;
    }

    return s3Url.substring(s3Url.indexOf("amazonaws.com/") + "amazonaws.com/".length());
  }

  private void updateSummary(
          Long memId, Long projectId,
          PipeLineStatus pipeLineStatus,
          List<ItemDashboardSummary> summaryList
  ) {
    Map<String, Integer> countMap = null;

    switch (pipeLineStatus) {
      case SEARCH:
        ServiceResponse<PipeTaskGetResponse<PipeTaskSearch>> searchResult = this.getSearchTasks(memId, projectId);
        if (searchResult.isSuccess()) {
          countMap = searchResult.getData().getCount();
        }
        break;
      case CLEAN:
        ServiceResponse<PipeTaskGetResponse<PipeTaskCleanResponse>> cleanResult = this.getCleanTasks(memId, projectId);
        if (cleanResult.isSuccess()) {
          countMap = cleanResult.getData().getCount();
        }
        break;
      case FREQUENCY:
        ServiceResponse<PipeTaskGetResponse<PipeTaskFrequencyResponse>> frequencyResult = this.getFrequencyTasks(memId, projectId);
        if (frequencyResult.isSuccess()) {
          countMap = frequencyResult.getData().getCount();
        }
        break;
      case TFIDF:
        ServiceResponse<PipeTaskGetResponse<PipeTaskTfidfResponse>> tfidfResult = this.getTfidfTasks(memId, projectId);
        if (tfidfResult.isSuccess()) {
          countMap = tfidfResult.getData().getCount();
        }
        break;
      case CONCOR:
        ServiceResponse<PipeTaskGetResponse<PipeTaskConcorResponse>> concorResult = this.getConcorTasks(memId, projectId);
        if (concorResult.isSuccess()) {
          countMap = concorResult.getData().getCount();
        }
        break;
    }

    if (countMap != null) {
      for (ItemDashboardSummary summary : summaryList) {
        if (summary.getStatus() == pipeLineStatus) {
          summary.setTotal(countMap.get("total"));
          summary.setPreparing(countMap.getOrDefault(PipeTaskState.PREPARING.getValue(), 0));
          summary.setPending(countMap.getOrDefault(PipeTaskState.PENDING.getValue(), 0));
          summary.setProgress(countMap.getOrDefault(PipeTaskState.IN_PROGRESS.getValue(), 0));
          summary.setCompleted(countMap.getOrDefault(PipeTaskState.COMPLETED.getValue(), 0));
          break;
        }
      }
    }
  }

  private List<JoinedPipeTask> getJoinedTasks(Long memId, Long projectId) {
    try {
      Pageable pageable = PageRequest.of(0, 20);
      List<JoinedPipeTask> joinedTasks = pipeTaskSearchRepo.findTop20JoinedTasksByMemIdAndProjectId(memId, projectId, pageable);
      return joinedTasks;
    } catch (Exception e) {
      log.error("Failed to get joined tasks for member ID: {} and project ID: {}", memId, projectId, e);
      return new ArrayList<JoinedPipeTask>();
    }
  }


}
