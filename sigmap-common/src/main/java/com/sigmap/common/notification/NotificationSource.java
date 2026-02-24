package com.sigmap.common.notification;

/**
 * Identifies which module/entity originated a notification.
 * Extend this enum as new modules are added to the platform.
 */
public enum NotificationSource {
    PAA,
    PPM,
    DAC,
    AGPM,
    ECLAIRCISSEMENT,
    PARAMS,
    WEBMASTER,
    REUNION,
    SYSTEM
}
