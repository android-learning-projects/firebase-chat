package com.example.chatapp.notifications;

public class Constants {
    public static final String BOOK_NAME = "book_name";
    public static final String BOOK_CHAPTER_COUNT = "book_chapter_count";
    public static final String BOOK_ID = "book_id";
    public static final String SHARE_ID = "share_id";


    public static final String CALENDAR_URL = "https://www.arulvakku.com/calendar.php";
    public static final String DONATE_PAGE = "https://www.arulvakku.com/donate.php";

    private static final String BASE_URL = "http://arulvakku.binaryexpertsystems.com/Arulvakku/";
    public static final String GET_PRAYER_REQUEST_BY_DEVICE_ID = BASE_URL + "GetPrayerRequestByDeviceId";
    public static final String DELETE_PRAYER_REQUEST = BASE_URL + "DeletePrayerRequest";
    public static final String POST_PRAYER_REQUEST = BASE_URL + "InsertPrayerRequest";
    public static final String UPDATE_FIREBASE_KEY = BASE_URL + "UpdateFireBaseKey";
    public static final String INSERT_USER = BASE_URL + "InsertUser";
    public static final String GET_WAY_OF_THE_CROSS = BASE_URL + "GetWayOfTheCross";


    /*
     * Worker constants
     * */
    public static final String KEY_WORKER_ID = "worker_id";
    public static final String WORK_DAILY_NOTIFICATION = "work_daily_notification";

    /*
     * Notification constants
     * */
    public static final int DAILY_NOTIFICATION_ID = 101;
    public static final String DAILY_NOTIFICATION_CHANNEL_ID = "101";
    public static final String DAILY_NOTIFICATION_CHANNEL_NAME = "channel_daily_notification";
    public static final String ACTION_NOTIFICATION_SHARE = "action_share";

    public static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";

}
