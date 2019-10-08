package com.example.flashcall;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

public class JobSchedule extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intent = new Intent(getApplicationContext(), flash.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getApplicationContext().startForegroundService(intent);
        }else {
            getApplicationContext().startService(intent);
        }
        ScheduleUtils.ScheduleUtils(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
