package com.example.demo.job.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.job.dto.*;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.model.JobMisfirePolicy;
import com.example.demo.job.service.SysJobLogService;
import com.example.demo.job.service.SysJobService;
import com.example.demo.job.support.JobHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    private final JobHandlerRegistry jobHandlerRegistry;

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
            return error(404, i18n("job.not.found"));
        }
        return success(jobService.toView(job));
    }

    @PostMapping
    @RequirePermission("job:create")
    public CommonResult<JobVO> create(@Valid @RequestBody JobCreateRequest request) {
        String cron = request.getCronExpression();
        if (!CronExpression.isValidExpression(cron)) {
            return error(400, i18n("job.cron.invalid"));
        }
        if (jobHandlerRegistry.getHandler(request.getHandlerName()) == null) {
            return error(400, i18n("job.handler.invalid"));
        }
        if (StringUtils.isNotBlank(request.getMisfirePolicy())
                && !JobMisfirePolicy.isSupported(request.getMisfirePolicy())) {
            return error(400, i18n("job.misfire.invalid"));
        }
        AuthUser user = AuthContext.get();
        SysJob job = jobService.createJob(request, user);
        if (job == null) {
            return error(500, i18n("job.create.failed"));
        }
        return success(jobService.toView(job));
    }

    @PutMapping("/{id}")
    @RequirePermission("job:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody JobUpdateRequest request) {
        SysJob existing = jobService.getById(id);
        if (existing == null) {
            return error(404, i18n("job.not.found"));
        }
        if (StringUtils.isNotBlank(request.getCronExpression())
                && !CronExpression.isValidExpression(request.getCronExpression())) {
            return error(400, i18n("job.cron.invalid"));
        }
        if (StringUtils.isNotBlank(request.getHandlerName())
                && jobHandlerRegistry.getHandler(request.getHandlerName()) == null) {
            return error(400, i18n("job.handler.invalid"));
        }
        if (StringUtils.isNotBlank(request.getMisfirePolicy())
                && !JobMisfirePolicy.isSupported(request.getMisfirePolicy())) {
            return error(400, i18n("job.misfire.invalid"));
        }
        if (!jobService.updateJob(id, request)) {
            return error(500, i18n("job.update.failed"));
        }
        return success();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("job:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (jobService.getById(id) == null) {
            return error(404, i18n("job.not.found"));
        }
        if (!jobService.deleteJob(id)) {
            return error(500, i18n("job.delete.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("job:status")
    public CommonResult<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        if (jobService.getById(id) == null) {
            return error(404, i18n("job.not.found"));
        }
        if (!jobService.updateStatus(id, request.getStatus())) {
            return error(500, i18n("job.status.update.failed"));
        }
        return success();
    }

    @PutMapping("/{id}/run")
    @RequirePermission("job:run")
    public CommonResult<Void> runOnce(@PathVariable Long id) {
        if (jobService.getById(id) == null) {
            return error(404, i18n("job.not.found"));
        }
        if (!jobService.runOnce(id)) {
            return error(500, i18n("job.run.failed"));
        }
        return success();
    }

    @GetMapping("/{id}/logs")
    @RequirePermission("job:query")
    public CommonResult<PageResult<JobLogVO>> logs(@PathVariable Long id) {
        if (jobService.getById(id) == null) {
            return error(404, i18n("job.not.found"));
        }
        PageResult<com.example.demo.job.entity.SysJobLog> rawPage = page(new JobLogQuery(id), jobLogService::selectLogsPage);
        PageResult<JobLogVO> result = new PageResult<>(
                rawPage.getTotal(),
                jobLogService.toViewList(rawPage.getData()),
                rawPage.getPageNum(),
                rawPage.getPageSize());
        return success(result);
    }

    @GetMapping("/logs/{logId}")
    @RequirePermission("job:query")
    public CommonResult<JobLogDetailVO> logDetail(@PathVariable Long logId) {
        com.example.demo.job.entity.SysJobLog log = jobLogService.getById(logId);
        if (log == null) {
            return error(404, i18n("job.log.not.found"));
        }
        return success(jobLogService.toDetailView(log));
    }
}
