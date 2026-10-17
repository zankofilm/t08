package ir.scooljavanrood.madreseyar.teacher;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Pure-Java conversion between Persian solar dates and ISO Gregorian dates.
 * No Android ICU or third-party dependency, compatible with minSdk 26.
 * Uses the arithmetic year-break algorithm for the Persian calendar.
 */
final class JalaliDates {
    private JalaliDates() {}
    private static final int[] BREAKS = {
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    };
    private static int div(int a,int b) { return a / b; }
    private static int mod(int a,int b) { return a - div(a,b)*b; }

    private static int[] yearInfo(int jy) {
        if (jy < BREAKS[0] || jy >= BREAKS[BREAKS.length-1])
            throw new IllegalArgumentException("سال شمسی پشتیبانی نمی‌شود.");
        int gy = jy + 621, leapJ=-14, jp=BREAKS[0], jump=0;
        for (int i=1;i<BREAKS.length;i++) {
            int jm=BREAKS[i]; jump=jm-jp;
            if (jy < jm) break;
            leapJ += div(jump,33)*8 + div(mod(jump,33),4);
            jp=jm;
        }
        int n=jy-jp;
        leapJ += div(n,33)*8 + div(mod(n,33)+3,4);
        if (mod(jump,33)==4 && jump-n==4) leapJ++;
        int leapG=div(gy,4) - div((div(gy,100)+1)*3,4) - 150;
        int march=20 + leapJ - leapG;
        if (jump-n<6) n = n-jump + div(jump+4,33)*33;
        int leap=mod(mod(n+1,33)-1,4);
        if (leap==-1) leap=4;
        return new int[] {gy,march,leap};
    }
    private static LocalDate newYear(int jy) {
        int[] info=yearInfo(jy);
        return LocalDate.of(info[0],3,info[1]);
    }
    /** Empty date means that the homework has no deadline. */
    static String toGregorian(String date) {
        if (date==null || date.trim().isEmpty()) return "";
        String normalized=HomeworkRules.persianDate(date);
        String[] bits=normalized.split("/");
        int jy=Integer.parseInt(bits[0]);
        int jm=Integer.parseInt(bits[1]);
        int jd=Integer.parseInt(bits[2]);
        int max=jm<=6 ? 31 : jm<=11 ? 30 : (yearInfo(jy)[2]==0 ? 30 : 29);
        if (jd<1 || jd>max) throw new IllegalArgumentException("روز انتخاب‌شده برای این ماه شمسی معتبر نیست.");
        int offset=(jm<=6 ? (jm-1)*31 : 186+(jm-7)*30) + jd-1;
        return newYear(jy).plusDays(offset).toString();
    }
    /** Converts ISO yyyy-MM-dd or an ISO timestamp from the server to Jalali. */
    static String fromGregorian(String date) {
        if (date==null || date.length()<10) return "";
        try {
            LocalDate d=LocalDate.parse(date.substring(0,10));
            int jy=d.getYear()-621;
            if (d.isBefore(newYear(jy))) jy--;
            LocalDate start=newYear(jy);
            long day=ChronoUnit.DAYS.between(start,d);
            if (day<0 || day>=ChronoUnit.DAYS.between(start,newYear(jy+1))) return "";
            int jm=day<186 ? (int)(day/31)+1 : (int)((day-186)/30)+7;
            int jd=day<186 ? (int)(day%31)+1 : (int)((day-186)%30)+1;
            return String.format(Locale.ROOT,"%04d/%02d/%02d",jy,jm,jd);
        } catch (Exception ignored) { return ""; }
    }
}
