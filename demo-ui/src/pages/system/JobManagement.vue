<template>
  <div class="system-module">
    <div class="module-head">
      <div>
        <div class="module-title">{{ t("job.title") }}</div>
        <div class="module-sub">{{ t("job.subtitle") }}</div>
      </div>
      <div class="module-actions" @keyup.enter="handleSearch">
        <el-input v-model.trim="filters.name" :placeholder="t('job.filter.namePlaceholder')" clearable size="small"/>
        <el-input v-model.trim="filters.handlerName" :placeholder="t('job.filter.handlerPlaceholder')" clearable
                  size="small"/>
        <el-select v-model="filters.status" :placeholder="t('job.filter.statusPlaceholder')" class="filter-select-wide"
                   clearable size="small">
          <el-option :value="1" :label="t('job.dialog.statusEnabled')"/>
          <el-option :value="0" :label="t('job.dialog.statusDisabled')"/>
        </el-select>
        <el-button size="small" @click="handleSearch">{{ t("job.filter.search") }}</el-button>
        <el-button v-permission="'job:create'" size="small" type="primary" @click="openCreate">{{
            t("job.filter.create")
          }}
        </el-button>
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
              @change="(value) => handleStatusChange(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('job.table.action')" width="340">
        <template #default="{row}">
          <div class="action-buttons">
            <el-button v-permission="'job:update'" size="small" text @click="openEdit(row)">{{ t("job.table.edit") }}</el-button>
            <el-button v-permission="'job:run'" size="small" text @click="runOnce(row)">{{ t("job.table.run") }}</el-button>
            <el-button v-permission="'job:query'" size="small" text @click="openLogDrawer(row)">
              {{ t("job.table.history") }}
            </el-button>
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

    <el-drawer v-model="logDrawerVisible" :title="logDrawerTitle" direction="rtl" size="60%">
      <div class="job-log-panel">
        <div class="job-log-header">
          <div class="job-log-title">{{ logJob?.name || "-" }}</div>
          <div class="job-log-meta">
            <span>{{ logJob?.handlerName || "-" }}</span>
            <span class="job-log-meta-sep">|</span>
            <span>{{ logJob?.cronExpression || "-" }}</span>
          </div>
        </div>
        <div class="job-log-filters" @keyup.enter="handleLogSearch">
          <el-date-picker
              v-model="logFilters.timeRange"
              :end-placeholder="t('job.history.endPlaceholder')"
              :start-placeholder="t('job.history.startPlaceholder')"
              class="job-log-range"
              range-separator="~"
              size="small"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-select v-model="logFilters.status" :placeholder="t('job.history.statusPlaceholder')" class="filter-select-wide"
                     clearable
                     size="small">
            <el-option :label="t('job.history.statusRunning')" :value="0"/>
            <el-option :label="t('job.history.statusSuccess')" :value="1"/>
            <el-option :label="t('job.history.statusFailed')" :value="2"/>
          </el-select>
          <el-select v-model="logFilters.triggerType" :placeholder="t('job.history.triggerPlaceholder')" class="filter-select-wide"
                     clearable size="small">
            <el-option :label="t('job.history.triggerScheduled')" value="SCHEDULED"/>
            <el-option :label="t('job.history.triggerManual')" value="MANUAL"/>
          </el-select>
          <el-button size="small" @click="handleLogSearch">{{ t("job.filter.search") }}</el-button>
          <el-button size="small" @click="resetLogFilters">{{ t("job.history.reset") }}</el-button>
        </div>

        <el-table v-loading="logLoading" :data="logRecords" row-key="id" size="small">
          <el-table-column :label="t('job.history.startTime')" width="170">
            <template #default="{row}">
              {{ formatDateTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.status')" width="110">
            <template #default="{row}">
              <el-tag :type="logStatusTagType(row.status)" size="small">
                {{ logStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.duration')" width="120">
            <template #default="{row}">
              {{ formatDuration(row.durationMs) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.triggerType')" width="120">
            <template #default="{row}">
              {{ formatTriggerType(row.triggerType) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.triggerUser')" width="120">
            <template #default="{row}">
              {{ row.triggerUserName || "-" }}
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.fireTime')" width="170">
            <template #default="{row}">
              {{ formatDateTime(row.fireTime) }}
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.error')" min-width="200">
            <template #default="{row}">
              <span class="job-log-error">{{ row.errorMessage || "-" }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('job.history.action')" width="150">
            <template #default="{row}">
              <div class="action-buttons">
                <el-button size="small" text @click="openLogDetail(row)">{{ t("job.history.detail") }}</el-button>
                <el-button size="small" text @click="openLogContent(row)">{{ t("job.history.viewLog") }}</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="job-log-footer">
          <el-pagination
              :current-page="logPageNum"
              :page-size="logPageSize"
              :total="logTotal"
              layout="total, sizes, prev, pager, next"
              @current-change="handleLogPageChange"
              @size-change="handleLogSizeChange"
          />
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="recordDetailVisible" :title="t('job.history.detailTitle')" width="720px">
      <div class="job-log-detail">
        <div class="job-log-detail-grid">
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailStatus") }}</span>
            <span>{{ logStatusLabel(recordDetail?.status) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailDuration") }}</span>
            <span>{{ formatDuration(recordDetail?.durationMs) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailTrigger") }}</span>
            <span>{{ formatTriggerType(recordDetail?.triggerType) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailStart") }}</span>
            <span>{{ formatDateTime(recordDetail?.startTime) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailEnd") }}</span>
            <span>{{ formatDateTime(recordDetail?.endTime) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailFire") }}</span>
            <span>{{ formatDateTime(recordDetail?.fireTime) }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailInstance") }}</span>
            <span>{{ recordDetail?.fireInstanceId || "-" }}</span>
          </div>
          <div class="job-log-detail-item">
            <span class="job-log-detail-label">{{ t("job.history.detailScheduler") }}</span>
            <span>{{ recordDetail?.schedulerInstance || "-" }}</span>
          </div>
        </div>

        <div class="job-log-detail-block">
          <div class="job-log-detail-label">{{ t("job.history.detailParams") }}</div>
          <pre class="job-log-detail-content">{{ recordDetail?.params || "-" }}</pre>
        </div>

        <div class="job-log-detail-block">
          <div class="job-log-detail-label">{{ t("job.history.detailError") }}</div>
          <pre class="job-log-detail-content">{{ recordDetail?.errorStacktrace || "-" }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="recordDetailVisible = false">{{ t("common.cancel") }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logContentVisible" :title="logContentTitle" width="860px">
      <div class="job-log-content-panel">
        <div class="job-log-content-filters" @keyup.enter="handleLogContentSearch">
          <el-date-picker
              v-model="logContentFilters.timeRange"
              :end-placeholder="t('job.history.endPlaceholder')"
              :start-placeholder="t('job.history.startPlaceholder')"
              class="job-log-range"
              range-separator="~"
              size="small"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-select v-model="logContentFilters.level" :placeholder="t('job.history.levelPlaceholder')" class="filter-select-wide"
                     clearable size="small">
            <el-option :label="t('job.history.levelInfo')" value="INFO"/>
            <el-option :label="t('job.history.levelWarn')" value="WARN"/>
            <el-option :label="t('job.history.levelError')" value="ERROR"/>
            <el-option :label="t('job.history.levelDebug')" value="DEBUG"/>
          </el-select>
          <el-button size="small" @click="handleLogContentSearch">{{ t("job.filter.search") }}</el-button>
          <el-button size="small" @click="resetLogContentFilters">{{ t("job.history.reset") }}</el-button>
        </div>

        <el-scrollbar height="420px" @scroll="handleLogContentScroll">
          <div class="job-log-content-list">
            <div v-for="item in logContentRecords" :key="item.id" class="job-log-content-item">
              <div class="job-log-content-meta">
                <span :class="logLevelClass(item.logLevel)" class="job-log-content-level">
                  {{ formatLogLevel(item.logLevel) }}
                </span>
                <span class="job-log-content-time">{{ formatDateTimeRange(item.logStartTime, item.logEndTime) }}</span>
              </div>
              <pre class="job-log-content-text">{{ item.logContent || "-" }}</pre>
            </div>
            <div v-if="!logContentRecords.length && !logContentLoading" class="job-log-content-empty">
              {{ t("job.history.logEmpty") }}
            </div>
            <div v-if="logContentLoading" class="job-log-content-loading">
              {{ t("job.history.loading") }}
            </div>
            <div v-else-if="logContentFinished && logContentRecords.length" class="job-log-content-finished">
              {{ t("job.history.noMore") }}
            </div>
          </div>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button @click="logContentVisible = false">{{ t("common.cancel") }}</el-button>
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
  type JobCreatePayload,
  type JobHandlerInfo,
  type JobLogDetailVO,
  type JobLogVO,
  type JobUpdatePayload,
  type JobVO,
  listJobHandlers,
  listJobLogDetails,
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

const logDrawerVisible = ref(false);
const logLoading = ref(false);
const logRecords = ref<JobLogVO[]>([]);
const logPageNum = ref(1);
const logPageSize = ref(10);
const logTotal = ref(0);
const logJob = ref<JobVO | null>(null);
const recordDetailVisible = ref(false);
const recordDetail = ref<JobLogVO | null>(null);

const logContentVisible = ref(false);
const logContentTarget = ref<JobLogVO | null>(null);
const logContentRecords = ref<JobLogDetailVO[]>([]);
const logContentLoading = ref(false);
const logContentFinished = ref(false);
const logContentPageNum = ref(1);
const logContentPageSize = ref(50);

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
  remark: ""
});

const logFilters = reactive({
  status: undefined as number | undefined,
  triggerType: "",
  timeRange: null as string[] | null
});

const logContentFilters = reactive({
  level: "",
  timeRange: null as string[] | null
});

const editorTitle = computed(() =>
    editorMode.value === "create" ? t("job.dialog.createTitle") : t("job.dialog.editTitle")
);

const logDrawerTitle = computed(() => {
  if (logJob.value?.name) {
    return `${t("job.history.title")} - ${logJob.value.name}`;
  }
  return t("job.history.title");
});

const logContentTitle = computed(() => {
  if (logContentTarget.value?.id != null) {
    return `${t("job.history.logTitle")} #${logContentTarget.value.id}`;
  }
  return t("job.history.logTitle");
});

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

async function handleStatusChange(row: JobVO, value: string | number | boolean) {
  const nextStatus = value === true ? 1 : value === false ? 0 : Number(value);
  const result = await updateJobStatus(row.id, nextStatus);
  if (result?.code === 200) {
    row.status = nextStatus;
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

function openLogDrawer(row: JobVO) {
  logJob.value = row;
  logDrawerVisible.value = true;
  logPageNum.value = 1;
  loadJobLogs();
}

function handleLogSearch() {
  logPageNum.value = 1;
  loadJobLogs();
}

function handleLogPageChange(value: number) {
  logPageNum.value = value;
  loadJobLogs();
}

function handleLogSizeChange(value: number) {
  logPageSize.value = value;
  logPageNum.value = 1;
  loadJobLogs();
}

function resetLogFilters() {
  logFilters.status = undefined;
  logFilters.triggerType = "";
  logFilters.timeRange = null;
  handleLogSearch();
}

function openLogDetail(row: JobLogVO) {
  recordDetail.value = row;
  recordDetailVisible.value = true;
}

function openLogContent(row: JobLogVO) {
  logContentTarget.value = row;
  logContentVisible.value = true;
  logContentPageNum.value = 1;
  logContentFinished.value = false;
  logContentRecords.value = [];
  loadLogContent(true);
}

function handleLogContentSearch() {
  logContentPageNum.value = 1;
  logContentFinished.value = false;
  logContentRecords.value = [];
  loadLogContent(true);
}

function resetLogContentFilters() {
  logContentFilters.level = "";
  logContentFilters.timeRange = null;
  handleLogContentSearch();
}

function handleLogContentScroll(payload: { scrollTop: number; scrollHeight: number; clientHeight: number }) {
  if (logContentLoading.value || logContentFinished.value) {
    return;
  }
  const {scrollTop, scrollHeight, clientHeight} = payload;
  if (scrollTop + clientHeight >= scrollHeight - 24) {
    logContentPageNum.value += 1;
    loadLogContent(false);
  }
}

function formatLogLevel(level?: string) {
  const value = String(level || "").trim().toUpperCase();
  if (!value) {
    return "-";
  }
  if (value === "INFO") {
    return t("job.history.levelInfo");
  }
  if (value === "WARN") {
    return t("job.history.levelWarn");
  }
  if (value === "ERROR") {
    return t("job.history.levelError");
  }
  if (value === "DEBUG") {
    return t("job.history.levelDebug");
  }
  return value;
}

function logLevelClass(level?: string) {
  const value = String(level || "").trim().toLowerCase();
  return value ? `job-log-content-level-${value}` : "";
}

function logStatusLabel(status?: number) {
  if (status === 0) {
    return t("job.history.statusRunning");
  }
  if (status === 1) {
    return t("job.history.statusSuccess");
  }
  if (status === 2) {
    return t("job.history.statusFailed");
  }
  return "-";
}

function logStatusTagType(status?: number) {
  if (status === 1) {
    return "success";
  }
  if (status === 2) {
    return "danger";
  }
  return "warning";
}

function formatTriggerType(type?: string) {
  const normalized = String(type || "").trim().toUpperCase();
  if (!normalized) {
    return "-";
  }
  if (normalized === "MANUAL") {
    return t("job.history.triggerManual");
  }
  if (normalized === "SCHEDULED") {
    return t("job.history.triggerScheduled");
  }
  return normalized;
}

function formatDuration(value?: number) {
  if (value == null) {
    return "-";
  }
  const ms = Math.max(Math.floor(value), 0);
  if (ms < 1000) {
    return `${ms} ms`;
  }
  const seconds = ms / 1000;
  if (seconds < 60) {
    return `${seconds.toFixed(2)} s`;
  }
  const minutes = Math.floor(seconds / 60);
  const remainSeconds = Math.round(seconds % 60);
  return `${minutes}m ${remainSeconds}s`;
}

function formatDateTimeRange(start?: string, end?: string) {
  const startText = formatDateTime(start);
  const endText = formatDateTime(end);
  if (startText === "-" && endText === "-") {
    return "-";
  }
  return `${startText} ~ ${endText}`;
}

async function loadJobLogs() {
  if (logLoading.value || !logJob.value) {
    return;
  }
  logLoading.value = true;
  const [startTimeFrom, startTimeTo] = logFilters.timeRange || [];
  try {
    const result = await listJobLogs(logJob.value.id, {
      pageNum: logPageNum.value,
      pageSize: logPageSize.value,
      startTimeFrom: startTimeFrom || undefined,
      startTimeTo: startTimeTo || undefined,
      status: logFilters.status,
      triggerType: logFilters.triggerType || undefined
    });
    if (result?.code === 200 && result.data) {
      logRecords.value = result.data.data || [];
      logTotal.value = result.data.total || 0;
    } else {
      ElMessage.error(result?.message || t("job.history.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("job.history.loadFailed"));
  } finally {
    logLoading.value = false;
  }
}

async function loadLogContent(resetList: boolean) {
  if (logContentLoading.value || !logContentTarget.value || !logJob.value) {
    return;
  }
  logContentLoading.value = true;
  const [logTimeFrom, logTimeTo] = logContentFilters.timeRange || [];
  try {
    const result = await listJobLogDetails(logJob.value.id, logContentTarget.value.id, {
      pageNum: logContentPageNum.value,
      pageSize: logContentPageSize.value,
      logLevel: logContentFilters.level || undefined,
      logTimeFrom: logTimeFrom || undefined,
      logTimeTo: logTimeTo || undefined
    });
    if (result?.code === 200 && result.data) {
      const next = result.data.data || [];
      if (resetList) {
        logContentRecords.value = next;
      } else {
        logContentRecords.value = logContentRecords.value.concat(next);
      }
      const total = result.data.total || 0;
      logContentFinished.value = logContentRecords.value.length >= total || next.length === 0;
    } else {
      ElMessage.error(result?.message || t("job.history.loadFailed"));
    }
  } catch (error) {
    ElMessage.error(t("job.history.loadFailed"));
  } finally {
    logContentLoading.value = false;
  }
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

watch(logDrawerVisible, (visible) => {
  if (!visible) {
    logRecords.value = [];
    logTotal.value = 0;
    recordDetailVisible.value = false;
    recordDetail.value = null;
    logContentVisible.value = false;
    logContentTarget.value = null;
    logContentRecords.value = [];
    logContentFinished.value = false;
    logContentLoading.value = false;
    logContentPageNum.value = 1;
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

.filter-select-wide {
  width: 120px;
  min-width: 100px;
}

.module-footer {
  display: flex;
  justify-content: flex-end;
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

.job-log-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-log-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.job-log-title {
  font-size: 16px;
  font-weight: 600;
}

.job-log-meta {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  flex-wrap: wrap;
}

.job-log-meta-sep {
  color: var(--el-border-color);
}

.job-log-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.job-log-range {
  min-width: 260px;
}

.job-log-footer {
  display: flex;
  justify-content: flex-end;
}

.job-log-error {
  color: var(--el-text-color-secondary);
}

.job-log-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-log-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
}

.job-log-detail-item {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
}

.job-log-detail-label {
  color: var(--el-text-color-secondary);
  min-width: 88px;
}

.job-log-detail-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.job-log-detail-content {
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 8px 10px;
  white-space: pre-wrap;
  font-size: 12px;
  margin: 0;
}

.job-log-content-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.job-log-content-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.job-log-content-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 2px;
}

.job-log-content-item {
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
}

.job-log-content-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.job-log-content-level {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 11px;
  background: var(--el-color-info-light-9);
  color: var(--el-color-info);
}

.job-log-content-level-warn {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.job-log-content-level-error {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.job-log-content-level-debug {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.job-log-content-text {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  color: var(--el-text-color-primary);
}

.job-log-content-empty,
.job-log-content-loading,
.job-log-content-finished {
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  padding: 8px 0;
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

  .job-log-detail-grid {
    grid-template-columns: 1fr;
  }

  .job-log-range {
    width: 100%;
  }
}
</style>
