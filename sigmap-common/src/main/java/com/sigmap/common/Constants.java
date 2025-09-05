package com.sigmap.common;

import java.time.LocalDate;

public class Constants {
    public static final String PATH_ENTREPOT_GED_PAA = "/SIGMAP-DEV/ARCHIVE/PAA/" + LocalDate.now().getYear();
    public static final String PATH_ENTREPOT_GED_PPM = "/SIGMAP-DEV/ARCHIVE/PPM/" + LocalDate.now().getYear();
    public static final String PATH_ENTREPOT_GED_AGPM = "/SIGMAP-DEV/ARCHIVE/AGPM/" + LocalDate.now().getYear();
    public static final String PATH_ENTREPOT_GED_REALISATION = "/SIGMAP-DEV/ARCHIVE/REALISATION/" + LocalDate.now().getYear();
    private Constants() {}}