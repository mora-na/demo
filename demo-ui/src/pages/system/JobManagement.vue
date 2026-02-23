<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("job.title") }}</div>
        <div class="module-sub">{{ t("job.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.name" clearable :placeholder="t('job.filter.namePlaceholder')"/>
        <el-input v-model.trim="filters.handlerName" clearable :placeholder="t('job.filter.handlerPlaceholder')"/>
        <el-select v-model="filters.status" clearable :placeholder="t('job.filter.statusPlaceholder')" style="width: 120px">
          <el-option :value="1" :label="t('job.dialog.statusEnabled')"/>
          <el-option :value="0" :label="t('job.dialog.statusDisabled')"/>
        </el-select>
        <el-button @click="handleSearch">{{ t("job.filter.search") }}</el-button>
        <el-button v-permission="'job:create'" type="primary" @click="openCreate">{{ t("job.filter.create") }}</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="jobs" row-key="id" size="small">
      <el-table-column :label="t('job.table.name')" min-width="160" prop="name"/>
      <el-table-column :label="t('job.table.handler')" min-width="160" prop="handlerName"/>
      <el-table-column :label="t('job.table.cron')" min-width="160" prop="cronExpression"/>
      <el-table-column :label="t('job.table.nextFireTime')" width="170">
        <template #default="{row}">
          {{ formatDateTime(row.nextFireTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('job.table.concurrent')" width="80">
        <template #default="{row}">
          {{ row.allowConcurrent === 0 ? t("job.table.no") : t("job.table.yes") }}
        </template>
      </el-table-column>
      <el-table-column :label="t('job.table.status')" width="110">
        <template #default="{row}">
          <el-switch
              v-permission="'job:status'"
              :active-value="1"
              :inactive-value="0"
              :model-value="row.status ?? 0"
              @change="(value: number) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('job.table.action')" width="280">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'job:update'" size="small" text @click="openEdit(row)">{{ t("job.table.edit") }}</el-button>
            <el-button v-permission="'job:run'" size="small" text @click="runOnce(row)">{{ t("job.table.run") }}</el-button>
            <el-button size="small" text @click="openLogs(row)">{{ t("job.table.logs") }}</el-button>
            <el-button v-permission="'job:delete'" size="small" text type="danger" @click="removeJob(row)">{{ t("job.table.delete") }}</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="module-footer">
      <el-pagination
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="editorVisible" :title="editorTitle" align-center width="720px">
      <el-form :model="form" label-position="top" class="job-editor-form">
        <el-form-item :label="t('job.dialog.name')">
          <el-input v-model.trim="form.name" :placeholder="t('job.dialog.namePlaceholder')"/>
        </el-form-item>
        <el-form-item :label="t('job.dialog.handler')">
          <el-select v-model="form.handlerName" :placeholder="t('job.dialog.handlerPlaceholder')">
            <el-option
                v-for="handler in handlers"
                :key="handler.name"
                :label="handler.name"
                :value="handler.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('job.dialog.cron')">
          <el-input v-model.trim="form.cronExpression" :placeholder="t('job.dialog.cronPlaceholder')">
            <template #append>
              <el-button @click="openCronHelper">{{ t("job.cronHelper.open") }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="t('job.dialog.misfire')">
          <el-select v-model="form.misfirePolicy" :placeholder="t('job.dialog.misfirePlaceholder')">
            <el-option :label="t('job.dialog.misfireDefault')" value="DEFAULT"/>
            <el-option :label="t('job.dialog.misfireIgnore')" value="IGNORE_MISFIRE"/>
            <el-option :label="t('job.dialog.misfireFire')" value="FIRE_AND_PROCEED"/>
            <el-option :label="t('job.dialog.misfireDoNothing')" value="DO_NOTHING"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('job.dialog.logCollectLevel')">
          <el-select v-model="form.logCollectLevel" :placeholder="t('job.dialog.logCollectLevelPlaceholder')">
            <el-option
                v-for="level in logCollectLevelOptions"
                :key="level"
                :label="level"
                :value="level"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('job.dialog.concurrent')">
          <el-select v-model="form.allowConcurrent" :placeholder="t('job.dialog.concurrentPlaceholder')">
            <el-option :value="1" :label="t('job.dialog.concurrentYes')"/>
            <el-option :value="0" :label="t('job.dialog.concurrentNo')"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('job.dialog.status')">
          <el-select v-model="form.status" :placeholder="t('job.dialog.statusPlaceholder')">
            <el-option :value="1" :label="t('job.dialog.statusEnabled')"/>
            <el-option :value="0" :label="t('job.dialog.statusDisabled')"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="t('job.dialog.params')" class="full-row">
          <el-input v-model.trim="form.params" :rows="3" :placeholder="t('job.dialog.paramsPlaceholder')" type="textarea"/>
        </el-form-item>
        <el-form-item :label="t('job.dialog.remark')" class="full-row">
          <el-input v-model.trim="form.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button v-permission="editorMode === 'create' ? 'job:create' : 'job:update'" :loading="saving" type="primary" @click="saveJob">{{ t("common.save") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" :title="t('job.logs.title')" align-center width="850px">
      <el-table v-loading="logLoading" :data="logs" row-key="id" size="small" style="width: 100%">
        <el-table-column :label="t('job.logs.task')" prop="jobName" width="120" show-overflow-tooltip/>
        <el-table-column :label="t('job.logs.handler')" prop="handlerName" width="120" show-overflow-tooltip/>
        <el-table-column :label="t('job.logs.status')" width="70">
          <template #default="{row}">
            {{ row.status === 1 ? t("job.logs.statusSuccess") : t("job.logs.statusFail") }}
          </template>
        </el-table-column>
        <el-table-column :label="t('job.logs.startTime')" width="140">
          <template #default="{row}">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('job.logs.duration')" width="80">
          <template #default="{row}">
            {{ row.durationMs ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column :label="t('job.logs.message')" prop="message" width="180" show-overflow-tooltip/>
        <el-table-column :label="t('job.logs.action')" width="90">
          <template #default="{row}">
            <el-button size="small" text @click="openLogDetail(row)">{{ t("job.logs.viewLog") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="module-footer">
        <el-pagination
            :current-page="logPageNum"
            :page-size="logPageSize"
            :total="logTotal"
            layout="total, sizes, prev, pager, next"
            @current-change="handleLogPageChange"
            @size-change="handleLogSizeChange"
        />
      </div>
      <template #footer>
        <el-button @click="logVisible = false">{{ t("job.logs.close") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logDetailVisible" :title="t('job.logs.detailTitle')" align-center width="840px">
      <div class="log-detail-body" v-loading="logDetailLoading">
        <div class="log-detail-meta">
          <span>{{ logDetail?.jobName || "-" }}</span>
          <span>{{ formatDateTime(logDetail?.startTime) }}</span>
          <span>{{ logDetail?.status === 1 ? t("job.logs.statusSuccess") : t("job.logs.statusFail") }}</span>
        </div>
        <pre class="log-detail-content">{{ logDetailText }}</pre>
      </div>
      <template #footer>
        <el-button @click="logDetailVisible = false">{{ t("job.logs.close") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cronHelperVisible" align-center :title="t('job.cronHelper.title')" width="640px">
      <el-form label-position="top" class="cron-helper">
        <div class="cron-helper-main">
          <el-form-item :label="t('job.cronHelper.template')">
            <el-select v-model="cronHelperTemplate" :placeholder="t('job.cronHelper.templatePlaceholder')">
              <el-option-group :label="t('job.cronHelper.groups.timePoint')">
                <el-option :label="t('job.cronHelper.templates.timeFixed')" value="time_fixed"/>
                <el-option :label="t('job.cronHelper.templates.timeMulti')" value="time_multi"/>
                <el-option :label="t('job.cronHelper.templates.timeRange')" value="time_range"/>
              </el-option-group>
              <el-option-group :label="t('job.cronHelper.groups.date')">
                <el-option :label="t('job.cronHelper.templates.dateMonthDays')" value="date_month_days"/>
                <el-option :label="t('job.cronHelper.templates.dateWeekdays')" value="date_weekdays"/>
                <el-option :label="t('job.cronHelper.templates.dateLastDay')" value="date_last_day"/>
                <el-option :label="t('job.cronHelper.templates.dateLastDayOffset')" value="date_last_day_offset"/>
                <el-option :label="t('job.cronHelper.templates.dateLastWeekday')" value="date_last_weekday"/>
                <el-option :label="t('job.cronHelper.templates.dateLastWeekdayOfMonth')"
                           value="date_last_weekday_of_month"/>
                <el-option :label="t('job.cronHelper.templates.dateNearestWeekday')" value="date_nearest_weekday"/>
                <el-option :label="t('job.cronHelper.templates.dateNthWeekday')" value="date_nth_weekday"/>
                <el-option :label="t('job.cronHelper.templates.dateYear')" value="date_year"/>
              </el-option-group>
              <el-option-group :label="t('job.cronHelper.groups.frequency')">
                <el-option :label="t('job.cronHelper.templates.freqSeconds')" value="freq_seconds"/>
                <el-option :label="t('job.cronHelper.templates.freqMinutes')" value="freq_minutes"/>
                <el-option :label="t('job.cronHelper.templates.freqHours')" value="freq_hours"/>
                <el-option :label="t('job.cronHelper.templates.freqDays')" value="freq_days"/>
                <el-option :label="t('job.cronHelper.templates.freqWeeks')" value="freq_weeks"/>
                <el-option :label="t('job.cronHelper.templates.freqMonths')" value="freq_months"/>
                <el-option :label="t('job.cronHelper.templates.freqYears')" value="freq_years"/>
              </el-option-group>
              <el-option-group :label="t('job.cronHelper.groups.combo')">
                <el-option :label="t('job.cronHelper.templates.comboWorkdaysHours')" value="combo_workdays_hours"/>
                <el-option :label="t('job.cronHelper.templates.comboQuarterStart')" value="combo_quarter_start"/>
                <el-option :label="t('job.cronHelper.templates.comboRangeStep')" value="combo_range_step"/>
              </el-option-group>
            </el-select>
          </el-form-item>

        <el-form-item v-if="cronHelperTemplate === 'freq_seconds'" :label="t('job.cronHelper.intervalSeconds')">
          <el-input-number v-model="cronHelper.intervalSeconds" :min="1" :max="59"/>
        </el-form-item>

        <el-form-item v-if="cronHelperTemplate === 'freq_minutes'" :label="t('job.cronHelper.intervalMinutes')">
          <el-input-number v-model="cronHelper.intervalMinutes" :min="1" :max="59"/>
        </el-form-item>

        <el-form-item v-if="cronHelperTemplate === 'freq_hours'" :label="t('job.cronHelper.intervalHours')">
          <el-input-number v-model="cronHelper.intervalHours" :min="1" :max="24"/>
        </el-form-item>

        <el-form-item v-if="cronHelperTemplate === 'freq_days'" :label="t('job.cronHelper.intervalDays')">
          <el-input-number v-model="cronHelper.intervalDays" :max="31" :min="1"/>
        </el-form-item>

        <div v-if="cronHelperTemplate === 'freq_weeks'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.intervalWeeks')" class="cron-item">
            <el-input-number v-model="cronHelper.intervalWeeks" :max="7" :min="1"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.weekday')" class="cron-item">
            <el-select v-model="cronHelper.weekday" :placeholder="t('job.cronHelper.weekdayPlaceholder')">
              <el-option :label="t('job.cronHelper.weekdays.mon')" value="MON"/>
              <el-option :label="t('job.cronHelper.weekdays.tue')" value="TUE"/>
              <el-option :label="t('job.cronHelper.weekdays.wed')" value="WED"/>
              <el-option :label="t('job.cronHelper.weekdays.thu')" value="THU"/>
              <el-option :label="t('job.cronHelper.weekdays.fri')" value="FRI"/>
              <el-option :label="t('job.cronHelper.weekdays.sat')" value="SAT"/>
              <el-option :label="t('job.cronHelper.weekdays.sun')" value="SUN"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
            <div class="cron-inline">
              <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
              <span class="cron-sep">:</span>
              <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
            </div>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'freq_months'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.intervalMonths')" class="cron-item">
            <el-input-number v-model="cronHelper.intervalMonths" :min="1" :max="12"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
            <el-input-number v-model="cronHelper.dayOfMonth" :min="1" :max="31"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
            <div class="cron-inline">
              <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
              <span class="cron-sep">:</span>
              <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
            </div>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'freq_years'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.months')" class="cron-item">
            <el-select v-model="cronHelper.yearlyMonth" :placeholder="t('job.cronHelper.monthsPlaceholder')">
              <el-option
                  v-for="option in monthOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
            <el-input-number v-model="cronHelper.yearlyDay" :min="1" :max="31"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
            <div class="cron-inline">
              <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
              <span class="cron-sep">:</span>
              <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
            </div>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'time_fixed'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.second')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedSecond" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'time_multi'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.hours')" class="cron-item">
            <el-select
                v-model="cronHelper.hours"
                :placeholder="t('job.cronHelper.hoursPlaceholder')"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option
                  v-for="option in hourOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minutes')" class="cron-item">
            <el-select
                v-model="cronHelper.minutes"
                :placeholder="t('job.cronHelper.minutesPlaceholder')"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option
                  v-for="option in minuteOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.second')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedSecond" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'time_range'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.rangeStartHour')" class="cron-item">
            <el-input-number v-model="cronHelper.rangeStartHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.rangeEndHour')" class="cron-item">
            <el-input-number v-model="cronHelper.rangeEndHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.rangeMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'date_month_days'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
            <el-select
                v-model="cronHelper.daysOfMonth"
                :placeholder="t('job.cronHelper.dayOfMonthPlaceholder')"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option
                  v-for="option in monthDayOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'date_weekdays'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.weekday')" class="cron-item">
            <el-select
                v-model="cronHelper.daysOfWeek"
                :placeholder="t('job.cronHelper.weekdayPlaceholder')"
                multiple
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option :label="t('job.cronHelper.weekdays.mon')" value="MON"/>
              <el-option :label="t('job.cronHelper.weekdays.tue')" value="TUE"/>
              <el-option :label="t('job.cronHelper.weekdays.wed')" value="WED"/>
              <el-option :label="t('job.cronHelper.weekdays.thu')" value="THU"/>
              <el-option :label="t('job.cronHelper.weekdays.fri')" value="FRI"/>
              <el-option :label="t('job.cronHelper.weekdays.sat')" value="SAT"/>
              <el-option :label="t('job.cronHelper.weekdays.sun')" value="SUN"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'date_last_day'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

          <div v-if="cronHelperTemplate === 'date_last_day_offset'" class="cron-row">
            <el-form-item :label="t('job.cronHelper.lastDayOffset')" class="cron-item">
              <el-input-number v-model="cronHelper.lastDayOffset" :max="30" :min="1"/>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
              <el-input-number v-model="cronHelper.fixedHour" :max="23" :min="0"/>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
              <el-input-number v-model="cronHelper.fixedMinute" :max="59" :min="0"/>
            </el-form-item>
          </div>

          <div v-if="cronHelperTemplate === 'date_last_weekday'" class="cron-row">
            <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
              <el-input-number v-model="cronHelper.fixedHour" :max="23" :min="0"/>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
              <el-input-number v-model="cronHelper.fixedMinute" :max="59" :min="0"/>
            </el-form-item>
          </div>

          <div v-if="cronHelperTemplate === 'date_last_weekday_of_month'" class="cron-row">
            <el-form-item :label="t('job.cronHelper.weekday')" class="cron-item">
              <el-select v-model="cronHelper.lastWeekday" :placeholder="t('job.cronHelper.weekdayPlaceholder')">
                <el-option :label="t('job.cronHelper.weekdays.mon')" value="MON"/>
                <el-option :label="t('job.cronHelper.weekdays.tue')" value="TUE"/>
                <el-option :label="t('job.cronHelper.weekdays.wed')" value="WED"/>
                <el-option :label="t('job.cronHelper.weekdays.thu')" value="THU"/>
                <el-option :label="t('job.cronHelper.weekdays.fri')" value="FRI"/>
                <el-option :label="t('job.cronHelper.weekdays.sat')" value="SAT"/>
                <el-option :label="t('job.cronHelper.weekdays.sun')" value="SUN"/>
              </el-select>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
              <div class="cron-inline">
                <el-input-number v-model="cronHelper.fixedHour" :max="23" :min="0"/>
                <span class="cron-sep">:</span>
                <el-input-number v-model="cronHelper.fixedMinute" :max="59" :min="0"/>
              </div>
            </el-form-item>
          </div>

        <div v-if="cronHelperTemplate === 'date_nearest_weekday'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
            <el-input-number v-model="cronHelper.nearestWeekday" :min="1" :max="31"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.hour')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'date_nth_weekday'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.weekday')" class="cron-item">
            <el-select v-model="cronHelper.nthWeekday" :placeholder="t('job.cronHelper.weekdayPlaceholder')">
              <el-option :label="t('job.cronHelper.weekdays.mon')" value="MON"/>
              <el-option :label="t('job.cronHelper.weekdays.tue')" value="TUE"/>
              <el-option :label="t('job.cronHelper.weekdays.wed')" value="WED"/>
              <el-option :label="t('job.cronHelper.weekdays.thu')" value="THU"/>
              <el-option :label="t('job.cronHelper.weekdays.fri')" value="FRI"/>
              <el-option :label="t('job.cronHelper.weekdays.sat')" value="SAT"/>
              <el-option :label="t('job.cronHelper.weekdays.sun')" value="SUN"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.nth')" class="cron-item">
            <el-input-number v-model="cronHelper.nthWeek" :min="1" :max="5"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
            <div class="cron-inline">
              <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
              <span class="cron-sep">:</span>
              <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
            </div>
          </el-form-item>
        </div>

          <div v-if="cronHelperTemplate === 'date_year'" class="cron-row">
            <el-form-item :label="t('job.cronHelper.year')" class="cron-item">
              <el-input-number v-model="cronHelper.yearlyYear" :max="2099" :min="1970"/>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.months')" class="cron-item">
              <el-select v-model="cronHelper.yearlyMonth" :placeholder="t('job.cronHelper.monthsPlaceholder')">
                <el-option
                    v-for="option in monthOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
              <el-input-number v-model="cronHelper.yearlyDay" :max="31" :min="1"/>
            </el-form-item>
            <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
              <div class="cron-inline">
                <el-input-number v-model="cronHelper.fixedHour" :max="23" :min="0"/>
                <span class="cron-sep">:</span>
                <el-input-number v-model="cronHelper.fixedMinute" :max="59" :min="0"/>
              </div>
            </el-form-item>
          </div>

        <div v-if="cronHelperTemplate === 'combo_workdays_hours'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.rangeStartHour')" class="cron-item">
            <el-input-number v-model="cronHelper.workdayStartHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.rangeEndHour')" class="cron-item">
            <el-input-number v-model="cronHelper.workdayEndHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.minute')" class="cron-item">
            <el-input-number v-model="cronHelper.workdayMinute" :min="0" :max="59"/>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'combo_quarter_start'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.months')" class="cron-item">
            <el-select
                v-model="cronHelper.quarterMonths"
                :placeholder="t('job.cronHelper.monthsPlaceholder')"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option
                  v-for="option in monthOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.dayOfMonth')" class="cron-item">
            <el-input-number v-model="cronHelper.quarterDay" :min="1" :max="31"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.time')" class="cron-item">
            <div class="cron-inline">
              <el-input-number v-model="cronHelper.fixedHour" :min="0" :max="23"/>
              <span class="cron-sep">:</span>
              <el-input-number v-model="cronHelper.fixedMinute" :min="0" :max="59"/>
            </div>
          </el-form-item>
        </div>

        <div v-if="cronHelperTemplate === 'combo_range_step'" class="cron-row">
          <el-form-item :label="t('job.cronHelper.stepMinutes')" class="cron-item">
            <el-input-number v-model="cronHelper.stepMinutes" :min="1" :max="59"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.rangeStartHour')" class="cron-item">
            <el-input-number v-model="cronHelper.stepStartHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.rangeEndHour')" class="cron-item">
            <el-input-number v-model="cronHelper.stepEndHour" :min="0" :max="23"/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.weekday')" class="cron-item">
            <el-select
                v-model="cronHelper.stepWeekdays"
                :placeholder="t('job.cronHelper.weekdayPlaceholder')"
                multiple
                collapse-tags
                collapse-tags-tooltip
            >
              <el-option :label="t('job.cronHelper.weekdays.mon')" value="MON"/>
              <el-option :label="t('job.cronHelper.weekdays.tue')" value="TUE"/>
              <el-option :label="t('job.cronHelper.weekdays.wed')" value="WED"/>
              <el-option :label="t('job.cronHelper.weekdays.thu')" value="THU"/>
              <el-option :label="t('job.cronHelper.weekdays.fri')" value="FRI"/>
              <el-option :label="t('job.cronHelper.weekdays.sat')" value="SAT"/>
              <el-option :label="t('job.cronHelper.weekdays.sun')" value="SUN"/>
            </el-select>
          </el-form-item>
        </div>

        </div>
        <div class="cron-helper-side">
          <el-form-item :label="t('job.cronHelper.preview')">
            <el-input :model-value="cronHelperPreview" class="cron-preview-input" readonly/>
          </el-form-item>
          <el-form-item :label="t('job.cronHelper.nextRuns')">
            <div v-loading="cronPreviewLoading" class="cron-preview-list">
              <div v-if="!cronHelperPreview" class="cron-preview-empty">
                {{ t("job.cronHelper.nextRunsEmpty") }}
              </div>
              <template v-else>
                <div v-if="!cronPreviewTimes.length" class="cron-preview-empty">
                  {{ t("job.cronHelper.nextRunsEmpty") }}
                </div>
                <div v-else>
                  <div
                      v-for="(item, index) in cronPreviewTimes"
                      :key="`${item}-${index}`"
                      class="cron-preview-item"
                  >
                    <span class="cron-preview-index">{{ index + 1 }}</span>
                    <span class="cron-preview-time">{{ item }}</span>
                  </div>
                </div>
                <div v-if="cronPreviewTimeZone" class="cron-preview-zone">
                  {{ t("job.cronHelper.timeZone") }}: {{ cronPreviewTimeZone }}
                </div>
              </template>
            </div>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="cronHelperVisible = false">{{ t("common.cancel") }}</el-button>
        <el-button :disabled="!cronHelperValid" type="primary" @click="applyCronHelper">
          {{ t("job.cronHelper.apply") }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import {useI18n} from "vue-i18n";
import {
  createJob,
  deleteJob,
  getJobLogDetail,
  type JobCreatePayload,
  type JobHandlerInfo,
  type JobLogDetailVO,
  type JobLogVO,
  type JobUpdatePayload,
  type JobVO,
  listJobHandlers,
  listJobLogs,
  listJobs,
  previewJobCron,
  runJob,
  updateJob,
  updateJobStatus,
} from "../../api/system";

const jobs = ref<JobVO[]>([]);
const handlers = ref<JobHandlerInfo[]>([]);
const loading = ref(false);
const saving = ref(false);
const editorVisible = ref(false);
const editorMode = ref<"create" | "edit">("create");
const editorId = ref<number | null>(null);
const {t} = useI18n();

const logs = ref<JobLogVO[]>([]);
const logVisible = ref(false);
const logLoading = ref(false);
const logPageNum = ref(1);
const logPageSize = ref(10);
const logTotal = ref(0);
const activeLogJobId = ref<number | null>(null);
const logDetailVisible = ref(false);
const logDetailLoading = ref(false);
const logDetail = ref<JobLogDetailVO | null>(null);

const logDetailText = computed(() => {
  const raw = logDetail.value?.logDetail;
  if (!raw) {
    return t("job.logs.emptyLog");
  }
  const decoded = decodeHtmlEntities(raw);
  return decoded.trim() ? decoded : t("job.logs.emptyLog");
});


const filters = reactive({
  name: "",
  handlerName: "",
  status: undefined as number | undefined
});

const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const form = reactive<JobCreatePayload & JobUpdatePayload>({
  name: "",
  handlerName: "",
  cronExpression: "",
  status: 1,
  allowConcurrent: 1,
  misfirePolicy: "DEFAULT",
  params: "",
  logCollectLevel: "INFO",
  remark: ""
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("job.dialog.createTitle") : t("job.dialog.editTitle")
);

const logCollectLevelOptions = ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"];

type CronTemplate =
    | "freq_seconds"
    | "freq_minutes"
    | "freq_hours"
    | "freq_days"
    | "freq_weeks"
    | "freq_months"
    | "freq_years"
    | "time_fixed"
    | "time_multi"
    | "time_range"
    | "date_month_days"
    | "date_weekdays"
    | "date_last_day"
    | "date_last_day_offset"
    | "date_last_weekday"
    | "date_last_weekday_of_month"
    | "date_nearest_weekday"
    | "date_nth_weekday"
    | "date_year"
    | "combo_workdays_hours"
    | "combo_quarter_start"
    | "combo_range_step";

const cronHelperVisible = ref(false);
const cronHelperTemplate = ref<CronTemplate>("freq_minutes");
const cronHelper = reactive({
  intervalSeconds: 10,
  intervalMinutes: 5,
  intervalHours: 1,
  intervalDays: 1,
  intervalWeeks: 1,
  intervalMonths: 1,
  fixedSecond: 0,
  fixedMinute: 0,
  fixedHour: 8,
  minutes: [0],
  hours: [8, 12, 18],
  rangeStartHour: 9,
  rangeEndHour: 17,
  rangeMinute: 0,
  dayOfMonth: 1,
  daysOfMonth: [1, 15],
  weekday: "MON",
  daysOfWeek: ["MON"],
  nearestWeekday: 15,
  nthWeekday: "SAT",
  nthWeek: 3,
  lastDayOffset: 1,
  lastWeekday: "MON",
  yearlyMonth: 1,
  yearlyDay: 1,
  yearlyYear: new Date().getFullYear(),
  workdayStartHour: 9,
  workdayEndHour: 18,
  workdayMinute: 0,
  quarterMonths: [1, 4, 7, 10],
  quarterDay: 1,
  stepMinutes: 10,
  stepStartHour: 8,
  stepEndHour: 12,
  stepWeekdays: ["MON", "TUE", "WED", "THU", "FRI"]
});

const cronPreviewTimes = ref<string[]>([]);
const cronPreviewTimeZone = ref("");
const cronPreviewLoading = ref(false);
let cronPreviewTimer: number | null = null;
let cronPreviewRequestSeq = 0;

const minuteOptions = computed(() => buildNumberOptions(0, 59));
const hourOptions = computed(() => buildNumberOptions(0, 23));
const monthDayOptions = computed(() => buildNumberOptions(1, 31));
const monthOptions = computed(() => buildNumberOptions(1, 12));

const cronHelperPreview = computed(() => {
  switch (cronHelperTemplate.value) {
    case "freq_seconds": {
      const interval = clampNumber(cronHelper.intervalSeconds, 1, 59);
      return `*/${interval} * * * * ?`;
    }
    case "freq_minutes": {
      const interval = clampNumber(cronHelper.intervalMinutes, 1, 59);
      return `0 */${interval} * * * ?`;
    }
    case "freq_hours": {
      const interval = clampNumber(cronHelper.intervalHours, 1, 24);
      return `0 0 */${interval} * * ?`;
    }
    case "freq_days": {
      const interval = clampNumber(cronHelper.intervalDays, 1, 31);
      return `0 0 0 */${interval} * ?`;
    }
    case "freq_weeks": {
      const interval = clampNumber(cronHelper.intervalWeeks, 1, 7);
      const weekday = normalizeWeekdaySingle(cronHelper.weekday) || "MON";
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ? * ${weekday}/${interval}`;
    }
    case "freq_months": {
      const interval = clampNumber(cronHelper.intervalMonths, 1, 12);
      const day = clampNumber(cronHelper.dayOfMonth, 1, 31);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${day} */${interval} ?`;
    }
    case "freq_years": {
      const month = clampNumber(cronHelper.yearlyMonth, 1, 12);
      const day = clampNumber(cronHelper.yearlyDay, 1, 31);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${day} ${month} ?`;
    }
    case "time_fixed": {
      const second = clampNumber(cronHelper.fixedSecond, 0, 59);
      return `${second} ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} * * ?`;
    }
    case "time_multi": {
      const minutes = normalizeNumberList(cronHelper.minutes, 0, 59);
      const hours = normalizeNumberList(cronHelper.hours, 0, 23);
      if (!minutes.length || !hours.length) {
        return "";
      }
      const second = clampNumber(cronHelper.fixedSecond, 0, 59);
      return `${second} ${listToCron(minutes, 0, 59)} ${listToCron(hours, 0, 23)} * * ?`;
    }
    case "time_range": {
      const [start, end] = normalizeRange(cronHelper.rangeStartHour, cronHelper.rangeEndHour, 0, 23);
      return `0 ${clampNumber(cronHelper.rangeMinute, 0, 59)} ${start}-${end} * * ?`;
    }
    case "date_month_days": {
      const days = normalizeNumberList(cronHelper.daysOfMonth, 1, 31);
      if (!days.length) {
        return "";
      }
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${listToCron(days, 1, 31)} * ?`;
    }
    case "date_weekdays": {
      const weekdays = normalizeWeekdayList(cronHelper.daysOfWeek);
      if (!weekdays.length) {
        return "";
      }
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ? * ${weekdays.join(",")}`;
    }
    case "date_last_day": {
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} L * ?`;
    }
    case "date_last_day_offset": {
      const offset = clampNumber(cronHelper.lastDayOffset, 1, 30);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} L-${offset} * ?`;
    }
    case "date_last_weekday": {
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} LW * ?`;
    }
    case "date_last_weekday_of_month": {
      const weekday = normalizeWeekdaySingle(cronHelper.lastWeekday) || "MON";
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ? * ${weekday}L`;
    }
    case "date_nearest_weekday": {
      const day = clampNumber(cronHelper.nearestWeekday, 1, 31);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${day}W * ?`;
    }
    case "date_nth_weekday": {
      const weekday = normalizeWeekdaySingle(cronHelper.nthWeekday) || "MON";
      const nth = clampNumber(cronHelper.nthWeek, 1, 5);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ? * ${weekday}#${nth}`;
    }
    case "date_year": {
      const year = clampNumber(cronHelper.yearlyYear, 1970, 2099);
      const month = clampNumber(cronHelper.yearlyMonth, 1, 12);
      const day = clampNumber(cronHelper.yearlyDay, 1, 31);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${day} ${month} ? ${year}`;
    }
    case "combo_workdays_hours": {
      const [start, end] = normalizeRange(cronHelper.workdayStartHour, cronHelper.workdayEndHour, 0, 23);
      return `0 ${clampNumber(cronHelper.workdayMinute, 0, 59)} ${start}-${end} ? * MON-FRI`;
    }
    case "combo_quarter_start": {
      const months = normalizeNumberList(cronHelper.quarterMonths, 1, 12);
      if (!months.length) {
        return "";
      }
      const day = clampNumber(cronHelper.quarterDay, 1, 31);
      return `0 ${clampNumber(cronHelper.fixedMinute, 0, 59)} ${clampNumber(cronHelper.fixedHour, 0, 23)} ${day} ${listToCron(months, 1, 12)} ?`;
    }
    case "combo_range_step": {
      const [start, end] = normalizeRange(cronHelper.stepStartHour, cronHelper.stepEndHour, 0, 23);
      const step = clampNumber(cronHelper.stepMinutes, 1, 59);
      const weekdays = normalizeWeekdayList(cronHelper.stepWeekdays);
      if (!weekdays.length) {
        return "";
      }
      return `0 */${step} ${start}-${end} ? * ${weekdays.join(",")}`;
    }
    default:
      return "";
  }
});

const cronHelperValid = computed(() => Boolean(cronHelperPreview.value));

function resetCronPreview() {
  cronPreviewTimes.value = [];
  cronPreviewTimeZone.value = "";
}

function scheduleCronPreview() {
  if (cronPreviewTimer != null) {
    window.clearTimeout(cronPreviewTimer);
  }
  cronPreviewTimer = window.setTimeout(() => {
    cronPreviewTimer = null;
    void loadCronPreview();
  }, 250);
}

async function loadCronPreview() {
  const expression = cronHelperPreview.value.trim();
  if (!cronHelperVisible.value || !expression) {
    resetCronPreview();
    return;
  }
  const requestSeq = ++cronPreviewRequestSeq;
  cronPreviewLoading.value = true;
  try {
    const result = await previewJobCron(expression);
    if (requestSeq !== cronPreviewRequestSeq) {
      return;
    }
    if (result?.code === 200 && result.data) {
      cronPreviewTimes.value = result.data.nextFireTimes || [];
      cronPreviewTimeZone.value = result.data.timeZone || "";
      return;
    }
    resetCronPreview();
  } catch (error) {
    if (requestSeq !== cronPreviewRequestSeq) {
      return;
    }
    resetCronPreview();
  } finally {
    if (requestSeq === cronPreviewRequestSeq) {
      cronPreviewLoading.value = false;
    }
  }
}

watch(cronHelperVisible, (visible) => {
  if (visible) {
    scheduleCronPreview();
    return;
  }
  resetCronPreview();
});

watch(cronHelperPreview, () => {
  if (!cronHelperVisible.value) {
    return;
  }
  scheduleCronPreview();
});

function formatDateTime(value?: string) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (num: number) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
      date.getHours()
  )}:${pad(date.getMinutes())}`;
}

function decodeHtmlEntities(value: string): string {
  if (!value.includes("&")) {
    return value;
  }
  if (typeof document === "undefined") {
    return value;
  }
  const textarea = document.createElement("textarea");
  textarea.innerHTML = value;
  return textarea.value;
}

function clampNumber(value: number, min: number, max: number) {
  if (!Number.isFinite(value)) {
    return min;
  }
  return Math.min(Math.max(Math.floor(value), min), max);
}

function buildNumberOptions(min: number, max: number) {
  const options = [];
  for (let value = min; value <= max; value += 1) {
    const label = String(value).padStart(2, "0");
    options.push({label, value});
  }
  return options;
}

function normalizeNumberList(values: number[], min: number, max: number) {
  const set = new Set<number>();
  for (const raw of values || []) {
    if (!Number.isFinite(raw)) {
      continue;
    }
    const value = Math.min(Math.max(Math.floor(raw), min), max);
    set.add(value);
  }
  return Array.from(set).sort((a, b) => a - b);
}

function listToCron(values: number[], min: number, max: number) {
  if (values.length === max - min + 1) {
    return "*";
  }
  return values.join(",");
}

function normalizeRange(start: number, end: number, min: number, max: number) {
  const safeStart = clampNumber(start, min, max);
  const safeEnd = clampNumber(end, min, max);
  return safeStart <= safeEnd ? [safeStart, safeEnd] : [safeEnd, safeStart];
}

function normalizeWeekdayList(values: string[]) {
  const allow = new Set(["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"]);
  const result: string[] = [];
  for (const raw of values || []) {
    const value = String(raw || "").trim().toUpperCase();
    if (allow.has(value) && !result.includes(value)) {
      result.push(value);
    }
  }
  return result;
}

function normalizeWeekdaySingle(value: string) {
  const allow = new Set(["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"]);
  const normalized = String(value || "").trim().toUpperCase();
  return allow.has(normalized) ? normalized : "";
}

function openCronHelper() {
  cronHelperVisible.value = true;
}

function applyCronHelper() {
  if (!cronHelperPreview.value) {
    ElMessage.warning(t("job.cronHelper.invalid"));
    return;
  }
  form.cronExpression = cronHelperPreview.value;
  cronHelperVisible.value = false;
}

async function loadHandlers() {
  const result = await listJobHandlers();
  if (result?.code === 200 && result.data) {
    handlers.value = result.data;
  }
}

async function loadJobs() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  try {
    const result = await listJobs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: filters.name || undefined,
      handlerName: filters.handlerName || undefined,
      status: filters.status
    });
    if (result?.code === 200 && result.data) {
      jobs.value = result.data.data || [];
      total.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("job.msg.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("job.msg.loadFailed"));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pageNum.value = 1;
  loadJobs();
}

function handlePageChange(value: number) {
  pageNum.value = value;
  loadJobs();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  pageNum.value = 1;
  loadJobs();
}

function resetForm() {
  form.name = "";
  form.handlerName = "";
  form.cronExpression = "";
  form.status = 1;
  form.allowConcurrent = 1;
  form.misfirePolicy = "DEFAULT";
  form.params = "";
  form.logCollectLevel = "INFO";
  form.remark = "";
}

function openCreate() {
  editorMode.value = "create";
  editorId.value = null;
  resetForm();
  editorVisible.value = true;
}

function openEdit(row: JobVO) {
  editorMode.value = "edit";
  editorId.value = row.id;
  form.name = row.name;
  form.handlerName = row.handlerName;
  form.cronExpression = row.cronExpression;
  form.status = row.status ?? 1;
  form.allowConcurrent = row.allowConcurrent ?? 1;
  form.misfirePolicy = row.misfirePolicy || "DEFAULT";
  form.params = row.params || "";
  form.logCollectLevel = row.logCollectLevel || "INFO";
  form.remark = row.remark || "";
  editorVisible.value = true;
}

async function saveJob() {
  if (!form.name.trim()) {
    ElMessage.warning(t("job.msg.validateName"));
    return;
  }
  if (!form.handlerName) {
    ElMessage.warning(t("job.msg.validateHandler"));
    return;
  }
  if (!form.cronExpression.trim()) {
    ElMessage.warning(t("job.msg.validateCron"));
    return;
  }
  saving.value = true;
  try {
    if (editorMode.value === "create") {
      const result = await createJob({...form});
      if (result?.code === 200) {
        ElMessage.success(t("job.msg.createSuccess"));
        editorVisible.value = false;
        await loadJobs();
      } else {
        ElMessage.error(result?.message || t("job.msg.createFailed"));
      }
    } else if (editorId.value != null) {
      const payload: JobUpdatePayload = {...form};
      const result = await updateJob(editorId.value, payload);
      if (result?.code === 200) {
        ElMessage.success(t("job.msg.updateSuccess"));
        editorVisible.value = false;
        await loadJobs();
      } else {
        ElMessage.error(result?.message || t("job.msg.updateFailed"));
      }
    }
  } catch (error) {
    ElMessage.error(t("job.msg.saveFailed"));
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(row: JobVO, value: number) {
  const result = await updateJobStatus(row.id, value);
  if (result?.code === 200) {
    row.status = value;
    ElMessage.success(t("job.msg.statusUpdated"));
    await loadJobs();
  } else {
    ElMessage.error(result?.message || t("job.msg.statusUpdateFailed"));
  }
}

async function runOnce(row: JobVO) {
  const result = await runJob(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("job.msg.runSuccess"));
  } else {
    ElMessage.error(result?.message || t("job.msg.runFailed"));
  }
}

async function removeJob(row: JobVO) {
  const result = await deleteJob(row.id);
  if (result?.code === 200) {
    ElMessage.success(t("job.msg.deleteSuccess"));
    await loadJobs();
  } else {
    ElMessage.error(result?.message || t("job.msg.deleteFailed"));
  }
}

async function openLogs(row: JobVO) {
  activeLogJobId.value = row.id;
  logVisible.value = true;
  logPageNum.value = 1;
  await loadLogs();
}

async function loadLogs() {
  if (logLoading.value || activeLogJobId.value == null) {
    return;
  }
  logLoading.value = true;
  try {
    const result = await listJobLogs(activeLogJobId.value, {
      pageNum: logPageNum.value,
      pageSize: logPageSize.value
    });
    if (result?.code === 200 && result.data) {
      logs.value = result.data.data || [];
      logTotal.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("job.msg.loadLogFailed"));
    }
  } catch (error) {
    ElMessage.error(t("job.msg.loadLogFailed"));
  } finally {
    logLoading.value = false;
  }
}

async function openLogDetail(row: JobLogVO) {
  if (row?.id == null) {
    return;
  }
  logDetailLoading.value = true;
  logDetailVisible.value = true;
  logDetail.value = null;
  try {
    const result = await getJobLogDetail(row.id);
    if (result?.code === 200) {
      logDetail.value = result.data || null;
    } else {
      ElMessage.error(result?.message || t("job.msg.loadLogDetailFailed"));
      logDetailVisible.value = false;
    }
  } catch (error) {
    ElMessage.error(t("job.msg.loadLogDetailFailed"));
    logDetailVisible.value = false;
  } finally {
    logDetailLoading.value = false;
  }
}

function handleLogPageChange(value: number) {
  logPageNum.value = value;
  loadLogs();
}

function handleLogSizeChange(value: number) {
  logPageSize.value = value;
  logPageNum.value = 1;
  loadLogs();
}

onMounted(() => {
  loadHandlers();
  loadJobs();
});

onUnmounted(() => {
  if (cronPreviewTimer != null) {
    window.clearTimeout(cronPreviewTimer);
    cronPreviewTimer = null;
  }
});
</script>

<style scoped>
.system-module {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
}

.module-sub {
  font-size: 12px;
  color: var(--muted);
}

.module-actions {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: 8px;
  align-items: center;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
}

.log-detail-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--muted);
}

.log-detail-content {
  margin: 0;
  padding: 12px;
  min-height: 220px;
  max-height: 420px;
  overflow-x: hidden;
  overflow-y: auto;
  border-radius: 12px;
  border: 1px solid rgba(18, 18, 18, 0.08);
  background: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  max-width: 100%;
}

.job-editor-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.job-editor-form :deep(.full-row) {
  grid-column: 1 / -1;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
}

.cron-helper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 0.6fr);
  gap: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  align-items: start;
}

.cron-helper-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cron-helper-side {
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: sticky;
  top: 0;
}

.cron-preview-input {
  width: 100%;
}

.cron-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  align-items: end;
}

.cron-inline {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px;
  border-radius: 10px;
  background: var(--el-fill-color-lighter);
  border: 1px dashed var(--el-border-color);
}

.cron-sep {
  color: var(--muted);
}

.cron-preview-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 10px;
  background: var(--el-fill-color-blank);
  min-height: 44px;
  width: 100%;
  box-sizing: border-box;
}

.cron-preview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  font-size: 12px;
}

.cron-preview-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-size: 12px;
  flex: 0 0 auto;
}

.cron-preview-time {
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}

.cron-preview-empty {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.cron-preview-zone {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

@media (max-width: 720px) {
  .job-editor-form {
    grid-template-columns: 1fr;
  }

  .cron-helper {
    grid-template-columns: 1fr;
  }

  .cron-helper-side {
    position: static;
  }

  .cron-row {
    grid-template-columns: 1fr;
  }
}
</style>
