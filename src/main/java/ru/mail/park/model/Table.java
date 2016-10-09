package ru.mail.park.model;

/**
 * Created by admin on 09.10.16.
 */
public class Table {

    public static final class Forum {
        public static final String TABLE_FORUM       = "`forum`.`Forum`";
        public static final String COLUMN_ID_FORUM   = "`forum`.`Forum`.`idForum`";
        public static final String COLUMN_NAME       = "`forum`.`Forum`.`name`";
        public static final String COLUMN_SHORT_NAME = "`forum`.`Forum`.`short_name`";
        public static final String COLUMN_SLUG       = "`forum`.`Forum`.`slug`";
        public static final String COLUMN_USER       = "`forum`.`Forum`.`user`";
    }

    public static final class Post {
        public static final String TABLE_POST = "`forum`.`Post`";
        public static final String COLUMN_ID_POST = "`forum`.`Post`.`idPost`";
    }

    public static final class Thread {
        public static final String TABLE_THREAD     = "`forum`.`Thread`";
        public static final String COLUMN_ID_THREAD = "`forum`.`Thread`.`idThread`";
    }

    public static final class User {
        public static final String TABLE_USER          = "`forum`.`User`";
        public static final String COLUMN_ID_USER      = "`forum`.`User`.`idUser`";
        public static final String COLUMN_USERNAME     = "`forum`.`User`.`username`";
        public static final String COLUMN_NAME         = "`forum`.`User`.`name`";
        public static final String COLUMN_EMAIL        = "`forum`.`User`.`email`";
        public static final String COLUMN_ABOUT        = "`forum`.`User`.`about`";
        public static final String COLUMN_IS_ANONYMOUS = "`forum`.`User`.`isAnonymous`";

    }

}
