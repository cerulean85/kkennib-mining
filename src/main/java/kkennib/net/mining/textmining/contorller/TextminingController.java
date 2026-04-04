package kkennib.net.mining.textmining.contorller;

import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.textmining.dto.*;
import kkennib.net.mining.textmining.entity.PipeTaskSearch;
import kkennib.net.mining.textmining.entity.StatefulTask;
import kkennib.net.mining.textmining.service.TextminingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/text-mining")
@RequiredArgsConstructor
public class TextminingController {
  private final TextminingService textminingService;

  @PostMapping("/create")
  public ServiceResponse<Map<String, List<? extends StatefulTask>>> login(@RequestBody Map<String, Object> request) {
    String keyword = (String) request.get("keyword");
    String startDateStr = (String) request.get("startDate");
    String endDateStr = (String) request.get("endDate");
    List<String> channel = (List<String>) request.get("channel");
    List<String> analysis = (List<String>) request.get("analysis");
    List<String> clean = (List<String>) request.get("clean");
    Long memId = Long.valueOf(request.get("memId").toString());
    Long projectId = Long.valueOf(request.get("projectId").toString());

    return textminingService.addNewWork(
            keyword,
            startDateStr,
            endDateStr,
            channel,
            clean,
            analysis,
            memId,
            projectId
    );
  }

  @GetMapping("/dashboard")
  public ServiceResponse<PipeTaskDashboardResponse> getDashboard(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getDashboardTasks(memId, projectId);
  }


  @GetMapping("/search-list")
  public ServiceResponse<PipeTaskGetResponse<PipeTaskSearch>> getSearchList(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getSearchTasks(memId, projectId);
  }

  @GetMapping("/clean-list")
  public ServiceResponse<PipeTaskGetResponse<PipeTaskCleanResponse>> getCleanList(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getCleanTasks(memId, projectId);
  }

  @DeleteMapping("/search-list")
  public ServiceResponse<Boolean> deleteSearchTasks(@RequestBody Map<String, Object> request) {
    List<Long> taskIdList = (List<Long>) request.get("taskIdList");
    return textminingService.deleteSearchTasks(taskIdList);
  }

  @GetMapping("/frequency-list")
  public ServiceResponse<PipeTaskGetResponse<PipeTaskFrequencyResponse>> getFrequencyList(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getFrequencyTasks(memId, projectId);
  }

  @DeleteMapping("/frequency-list")
  public ServiceResponse<Boolean> deleteFrequencyTasks(@RequestBody Map<String, Object> request) {
    List<Long> taskIdList = (List<Long>) request.get("taskIdList");
    return textminingService.deleteFrequencyTasks(taskIdList);
  }

  @GetMapping("/tfidf-list")
  public ServiceResponse<PipeTaskGetResponse<PipeTaskTfidfResponse>> getTfidfList(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getTfidfTasks(memId, projectId);
  }

  @DeleteMapping("/tfidf-list")
  public ServiceResponse<Boolean> deleteTfidfTasks(@RequestBody Map<String, Object> request) {
    List<Long> taskIdList = (List<Long>) request.get("taskIdList");
    return textminingService.deleteTfidfTasks(taskIdList);
  }

  @GetMapping("/concor-list")
  public ServiceResponse<PipeTaskGetResponse<PipeTaskConcorResponse>> getConcorList(
          @RequestParam("memId") Long memId,
          @RequestParam("projectId") Long projectId
  ) {
    return textminingService.getConcorTasks(memId, projectId);
  }

  @DeleteMapping("/concor-list")
  public ServiceResponse<Boolean> deleteConcorTasks(@RequestBody Map<String, Object> request) {
    List<Long> taskIdList = (List<Long>) request.get("taskIdList");
    return textminingService.deleteConcorTasks(taskIdList);
  }

  @GetMapping("/frequency-data")
  public ServiceResponse<PipeTaskPaginationResponse<ItemFrequency>> getFrequencyGraph(
          @RequestParam("taskId") Long id,
          @RequestParam(value = "page", defaultValue = "1") int page,
          @RequestParam(value = "size", defaultValue = "100") int size) {
    return textminingService.getFrequencyDataFromS3(id, page, size);
  }

  @GetMapping("/clean-data")
  public ServiceResponse<PipeTaskPaginationResponse<ItemClean>> getCleanData(
          @RequestParam("taskId") Long id,
          @RequestParam(value = "page", defaultValue = "1") int page,
          @RequestParam(value = "size", defaultValue = "100") int size) {
    return textminingService.getCleanDataFromS3(id, page, size);
  }

  @GetMapping("/tfidf-data")
  public ServiceResponse<PipeTaskPaginationResponse<ItemTfidf>> getTfidfData(
          @RequestParam("taskId") Long id,
          @RequestParam(value = "page", defaultValue = "1") int page,
          @RequestParam(value = "size", defaultValue = "100") int size) {
    return textminingService.getTfidfDataFromS3(id, page, size);
  }

  @GetMapping("/concor-data")
  public ServiceResponse<PipeTaskPaginationResponse<ItemConcor>> getConcorData(
          @RequestParam("taskId") Long id,
          @RequestParam(value = "page", defaultValue = "1") int page,
          @RequestParam(value = "size", defaultValue = "100") int size) {
    return textminingService.getConcorDataFromS3(id, page, size);
  }

}
