package org.sagebionetworks.bridge.sdk.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

import org.sagebionetworks.bridge.sdk.Config;
import org.sagebionetworks.bridge.sdk.rest.model.ABTestGroup;
import org.sagebionetworks.bridge.sdk.rest.model.ABTestScheduleStrategy;
import org.sagebionetworks.bridge.sdk.rest.model.Activity;
import org.sagebionetworks.bridge.sdk.rest.model.EmailTemplate;
import org.sagebionetworks.bridge.sdk.rest.model.MimeType;
import org.sagebionetworks.bridge.sdk.rest.model.Schedule;
import org.sagebionetworks.bridge.sdk.rest.model.SchedulePlan;
import org.sagebionetworks.bridge.sdk.rest.model.ScheduleType;
import org.sagebionetworks.bridge.sdk.rest.model.SimpleScheduleStrategy;
import org.sagebionetworks.bridge.sdk.rest.model.Study;
import org.sagebionetworks.bridge.sdk.rest.model.TaskReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Tests {
    
    private static final Config CONFIG = new Config();
    public static final String APP_NAME = "Integration Tests";
    public static final String TEST_KEY = "api";
    
    public static final EmailTemplate TEST_RESET_PASSWORD_TEMPLATE = new EmailTemplate().subject("Reset your password")
        .body("<p>${url}</p>").mimeType(MimeType.TEXT_HTML);
    public static final EmailTemplate TEST_VERIFY_EMAIL_TEMPLATE = new EmailTemplate().subject("Verify your email")
        .body("<p>${url}</p>").mimeType(MimeType.TEXT_HTML);

    public static String randomIdentifier(Class<?> cls) {
        return ("sdk-" + cls.getSimpleName().toLowerCase() + "-" + RandomStringUtils.randomAlphabetic(5)).toLowerCase();
    }

    public static String makeEmail(Class<?> cls) {
        String devName = CONFIG.getDevName();
        String clsPart = cls.getSimpleName();
        String rndPart = RandomStringUtils.randomAlphabetic(4);
        return String.format("bridge-testing+%s-%s-%s@sagebase.org", devName, clsPart, rndPart);
    }
    
    private static void setTaskActivity(Schedule schedule, String taskIdentifier) {
        checkNotNull(taskIdentifier);
        
        TaskReference ref = new TaskReference();
        ref.setIdentifier(taskIdentifier);

        Activity act = new Activity();
        act.setLabel("Task activity");
        act.setTask(ref);
        
        schedule.setActivities(Lists.newArrayList(act));
    }
    
    public static SchedulePlan getABTestSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("A/B Test Schedule Plan");
        Schedule schedule1 = new Schedule();
        schedule1.setScheduleType(ScheduleType.RECURRING);
        schedule1.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule1, "task:AAA");
        schedule1.setExpires("PT1H");
        schedule1.setLabel("Test label for the user");
        
        Schedule schedule2 = new Schedule();
        schedule2.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        schedule2.setScheduleType(ScheduleType.RECURRING);
        setTaskActivity(schedule2, "task:BBB");
        schedule2.setExpires("PT1H");
        schedule2.setLabel("Test label for the user");

        Schedule schedule3 = new Schedule();
        schedule3.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule3, "task:CCC");
        // This doesn't exist and now it matters, because we look for a survey to update the identifier
        // setSurveyActivity(schedule3, "identifier", "GUID-AAA", DateTime.parse("2015-01-27T17:46:31.237Z"));
        schedule3.setExpires("PT1H");
        schedule3.setLabel("Test label for the user");
        schedule3.setScheduleType(ScheduleType.RECURRING);

        ABTestScheduleStrategy strategy = new ABTestScheduleStrategy();
        strategy.setScheduleGroups(Lists.newArrayList(abGroup(40, schedule1), abGroup(40, schedule2), abGroup(20, schedule3)));
        strategy.setType("ABTestScheduleStrategy");
        
        plan.setStrategy(strategy);
        return plan;
    }
    
    private static ABTestGroup abGroup(int percentage, Schedule schedule) {
        ABTestGroup group = new ABTestGroup();
        group.setPercentage(percentage);
        group.setSchedule(schedule);
        return group;
    }
    
    public static SchedulePlan getSimpleSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Cron-based schedule");
        Schedule schedule = new Schedule();
        schedule.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule, "task:CCC");
        schedule.setExpires("PT1H");
        schedule.setLabel("Test label for the user");
        schedule.setScheduleType(ScheduleType.RECURRING);
        
        SimpleScheduleStrategy strategy = new SimpleScheduleStrategy();
        strategy.setSchedule(schedule);
        strategy.setType("SimpleScheduleStrategy");
        
        plan.setStrategy(strategy);
        return plan;
    }
    
    public static SchedulePlan getDailyRepeatingSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Daily repeating schedule plan");
        Schedule schedule = new Schedule();
        schedule.setLabel("Test label for the user");
        schedule.setScheduleType(ScheduleType.RECURRING);
        schedule.setInterval("P1D");
        schedule.setExpires("P1D");
        schedule.setTimes(Lists.newArrayList("12:00"));
        
        TaskReference taskReference = new TaskReference();
        taskReference.setIdentifier("task:CCC");
        
        Activity activity = new Activity();
        activity.setLabel("Task activity");
        activity.setTask(taskReference);
        schedule.setActivities(Lists.newArrayList(activity));
        
        SimpleScheduleStrategy strategy = new SimpleScheduleStrategy();
        strategy.setSchedule(schedule);
        strategy.setType("SimpleScheduleStrategy");
        
        plan.setStrategy(strategy);
        return plan;
    }
    
    public static SchedulePlan getPersistentSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Persistent schedule");
        Schedule schedule = new Schedule();
        setTaskActivity(schedule, "CCC");
        schedule.setEventId("task:"+schedule.getActivities().get(0).getTask().getIdentifier()+":finished");
        schedule.setLabel("Test label");
        schedule.setScheduleType(ScheduleType.PERSISTENT);

        SimpleScheduleStrategy strategy = new SimpleScheduleStrategy();
        strategy.setSchedule(schedule);
        strategy.setType("SimpleScheduleStrategy");
        
        plan.setStrategy(strategy);
        return plan;
    }

    public static Schedule getSimpleSchedule(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule();
    }
    
    public static List<Activity> getActivitiesFromSimpleStrategy(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule().getActivities();    
    }
    
    public static Activity getActivityFromSimpleStrategy(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule().getActivities().get(0);    
    }
    
    public static Study getStudy(String identifier, Integer version) {
        Study study = new Study();
        study.setIdentifier(identifier);
        study.setMinAgeOfConsent(18);
        study.setName("Test Study [SDK]");
        study.setSponsorName("The Test Study Folks [SDK]");
        study.setSupportEmail("test@test.com");
        study.setConsentNotificationEmail("test2@test.com");
        study.setTechnicalEmail("test3@test.com");
        study.setUsesCustomExportSchedule(true);
        study.getUserProfileAttributes().add("new_profile_attribute");
        study.setTaskIdentifiers(Lists.newArrayList("taskA")); // setting it differently just for the heck of it 
        study.setDataGroups(Lists.newArrayList("beta_users", "production_users"));
        study.setResetPasswordTemplate(Tests.TEST_RESET_PASSWORD_TEMPLATE);
        study.setVerifyEmailTemplate(Tests.TEST_VERIFY_EMAIL_TEMPLATE);
        study.setHealthCodeExportEnabled(Boolean.TRUE);
        
        Map<String,Integer> map = new HashMap<>();
        map.put("Android", 10);
        map.put("iPhone OS", 14);
        study.setMinSupportedAppVersions(map);
        if (version != null) {
            study.setVersion(version);
        }
        return study;
    }
    
    public static <T> boolean assertListsEqualIgnoringOrder(List<T> list1, List<T> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        Set<T> setA = Sets.newHashSet(list1);
        Set<T> setB = Sets.newHashSet(list2);
        return Sets.difference(setA, setB).isEmpty();
    }
}
