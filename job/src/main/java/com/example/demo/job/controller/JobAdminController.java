package com.example.demo.job.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.*;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.entity.SysJobLogDetail;
import com.example.demo.job.service.SysJobLogDetailService;
import com.example.demo.job.service.SysJobLogService;
import com.example.demo.job.service.SysJobService;
import com.example.demo.job.support.JobHandlerRegistry;
import com.example.demo.job.support.JobParamValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 定时任务管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Validated
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobAdminController extends BaseController {

    private final SysJobService jobService;
    private final SysJobLogService jobLogService;
    private final SysJobLogDetailService jobLogDetailService;
    private final JobHandlerRegistry jobHandlerRegistry;
    private final JobConstants jobConstants;
    private final JobParamValidator jobParamValidator;

    @GetMapping
    @RequirePermission("job:query")
    public CommonResult<PageResult<JobVO>> list(@ModelAttribute JobQuery query) {
        PageResult<SysJob> rawPage = page(query, jobService::selectJobsPage);
        List<JobVO> views = jobService.toViewList(rawPage.getData());
        PageResult<JobVO> result = new PageResult<>(rawPage.getTotal(), views, rawPage.getPageNum(), rawPage.getPageSize());
        return success(result);
    }

    @GetMapping("/handlers")
    @RequirePermission("job:query")
    public CommonResult<List<JobHandlerInfo>> handlers() {
        return success(jobHandlerRegistry.listHandlers());
    }

    @GetMapping("/{id}")
    @RequirePermission("job:query")
    public CommonResult<JobVO> detail(@PathVariable Long id) {
        SysJob job = jobService.getById(id);
        if (job == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        return success(jobService.toView(job));
    }

    @PostMapping
    @RequirePermission("job:create")
    public CommonResult<JobVO> create(@Valid @RequestBody JobCreateRequest request) {
        String cron = request.getCronExpression();
        if (!jobParamValidator.isValidCron(cron)) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobCronInvalid()));
        }
        if (!jobParamValidator.isValidHandler(request.getHandlerName())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobHandlerInvalid()));
        }
        if (!jobParamValidator.isValidMisfirePolicy(request.getMisfirePolicy())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobMisfireInvalid()));
        }
        if (!jobParamValidator.isValidStatus(request.getStatus())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobStatusInvalid()));
        }
        if (!jobParamValidator.isValidConcurrent(request.getAllowConcurrent())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobConcurrentInvalid()));
        }
        AuthUser user = AuthContext.get();
        SysJob job = jobService.createJob(request, user);
        if (job == null) {
            return error(jobConstants.getController().getInternalServerErrorCode(), i18n(jobConstants.getMessage().getJobCreateFailed()));
        }
        return success(jobService.toView(job));
    }

    @PutMapping("/{id}")
    @RequirePermission("job:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody JobUpdateRequest request) {
        SysJob existing = jobService.getById(id);
        if (existing == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        if (StringUtils.isNotBlank(request.getCronExpression())
                && !jobParamValidator.isValidCron(request.getCronExpression())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobCronInvalid()));
        }
        if (StringUtils.isNotBlank(request.getHandlerName())
                && !jobParamValidator.isValidHandler(request.getHandlerName())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobHandlerInvalid()));
        }
        if (!jobParamValidator.isValidMisfirePolicy(request.getMisfirePolicy())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobMisfireInvalid()));
        }
        if (request.getStatus() != null && !jobParamValidator.isValidStatus(request.getStatus())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobStatusInvalid()));
        }
        if (request.getAllowConcurrent() != null && !jobParamValidator.isValidConcurrent(request.getAllowConcurrent())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobConcurrentInvalid()));
        }
        if (!jobService.updateJob(id, request)) {
            return error(jobConstants.getController().getInternalServerErrorCode(), i18n(jobConstants.getMessage().getJobUpdateFailed()));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("job:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (jobService.getById(id) == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        if (!jobService.deleteJob(id)) {
            return error(jobConstants.getController().getInternalServerErrorCode(), i18n(jobConstants.getMessage().getJobDeleteFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("job:status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        if (jobService.getById(id) == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        if (!jobParamValidator.isValidStatus(request.getStatus())) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobStatusInvalid()));
        }
        if (!jobService.updateStatus(id, request.getStatus())) {
            return error(jobConstants.getController().getInternalServerErrorCode(), i18n(jobConstants.getMessage().getJobStatusUpdateFailed()));
        }
        return success();
    }

    @PutMapping("/{id}/run")
    @RequirePermission("job:run")
    public CommonResult<Void> runOnce(@PathVariable Long id) {
        if (jobService.getById(id) == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        AuthUser user = AuthContext.get();
        if (!jobService.runOnce(id, user)) {
            return error(jobConstants.getController().getInternalServerErrorCode(), i18n(jobConstants.getMessage().getJobRunFailed()));
        }
        return success();
    }

    @GetMapping("/{id}/logs")
    @RequirePermission("job:query")
    public CommonResult<PageResult<JobLogVO>> logs(@PathVariable Long id, @ModelAttribute JobLogQuery query) {
        if (jobService.getById(id) == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        JobLogQuery resolved = query == null ? new JobLogQuery() : query;
        resolved.setJobId(id);
        PageResult<SysJobLog> rawPage = page(resolved, jobLogService::selectJobLogsPage);
        List<JobLogVO> views = new ArrayList<>();
        for (SysJobLog log : rawPage.getData()) {
            JobLogVO view = jobLogService.toView(log);
            if (view != null) {
                views.add(view);
            }
        }
        PageResult<JobLogVO> result = new PageResult<>(rawPage.getTotal(), views, rawPage.getPageNum(), rawPage.getPageSize());
        return success(result);
    }

    @GetMapping("/{id}/logs/{logId}/details")
    @RequirePermission("job:query")
    public CommonResult<PageResult<JobLogDetailVO>> logDetails(@PathVariable Long id,
                                                               @PathVariable Long logId,
                                                               @ModelAttribute JobLogDetailQuery query) {
        if (jobService.getById(id) == null) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        SysJobLog log = jobLogService.getById(logId);
        if (log == null || log.getJobId() == null || !log.getJobId().equals(id)) {
            return error(jobConstants.getController().getNotFoundCode(), i18n(jobConstants.getMessage().getJobNotFound()));
        }
        JobLogDetailQuery resolved = query == null ? new JobLogDetailQuery() : query;
        resolved.setJobLogId(logId);
        PageResult<SysJobLogDetail> rawPage = page(resolved, jobLogDetailService::selectJobLogDetailsPage);
        List<JobLogDetailVO> views = new ArrayList<>();
        for (SysJobLogDetail detail : rawPage.getData()) {
            JobLogDetailVO view = jobLogDetailService.toView(detail);
            if (view != null) {
                views.add(view);
            }
        }
        PageResult<JobLogDetailVO> result = new PageResult<>(rawPage.getTotal(), views, rawPage.getPageNum(), rawPage.getPageSize());
        return success(result);
    }


    @PostMapping("/cron/preview")
    @RequirePermission("job:query")
    public CommonResult<JobCronPreviewVO> previewCron(@Valid @RequestBody JobCronPreviewRequest request) {
        String cron = request.getCronExpression();
        if (!jobParamValidator.isValidCron(cron)) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobCronInvalid()));
        }
        try {
            return success(buildCronPreview(cron.trim()));
        } catch (ParseException ex) {
            return error(jobConstants.getController().getBadRequestCode(), i18n(jobConstants.getMessage().getJobCronInvalid()));
        }
    }

    private JobCronPreviewVO buildCronPreview(String cronExpression) throws ParseException {
        CronExpression cron = new CronExpression(cronExpression);
        TimeZone timeZone = TimeZone.getDefault();
        cron.setTimeZone(timeZone);
        Date now = new Date();
        List<String> nextTimes = new ArrayList<>(5);
        Date next = cron.getNextValidTimeAfter(now);
        ZoneId zoneId = timeZone.toZoneId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int count = 0;
        while (next != null && count < 5) {
            ZonedDateTime time = ZonedDateTime.ofInstant(next.toInstant(), zoneId);
            nextTimes.add(formatter.format(time));
            next = cron.getNextValidTimeAfter(next);
            count += 1;
        }
        JobCronPreviewVO preview = new JobCronPreviewVO();
        preview.setCronExpression(cronExpression);
        preview.setTimeZone(zoneId.getId());
        preview.setNextFireTimes(nextTimes);
        return preview;
    }

}
