package ir.scooljavanrood.madreseyar.teacher;

/** Regression tests that run on GitHub without Android SDK classes. */
public final class TestJalaliDates {
    private static int checks=0;
    private static void eq(String input,String expected,boolean forward){
        String actual=forward ? JalaliDates.toGregorian(input) : JalaliDates.fromGregorian(input);
        if(!expected.equals(actual)) throw new AssertionError(input+" -> "+actual+" (wanted "+expected+")");
        checks++;
    }
    private static void reject(String date){
        boolean threw=false;
        try { JalaliDates.toGregorian(date); } catch(IllegalArgumentException e){ threw=true; }
        if(!threw) throw new AssertionError("Invalid date accepted: "+date);
        checks++;
    }
    public static void main(String[] args){
        eq("1405/01/01","2026-03-21",true);
        eq("1405/07/25","2026-10-17",true);
        eq("1403/01/01","2024-03-20",true);
        eq("1403/12/30","2025-03-20",true);
        eq("1404/01/01","2025-03-21",true);
        eq("1404/12/29","2026-03-20",true);
        eq("۱۴۰۵/۰۸/۲۵","2026-11-16",true);
        eq("","",true);
        eq("2026-03-21","1405/01/01",false);
        eq("2026-10-17","1405/07/25",false);
        eq("2024-03-20","1403/01/01",false);
        eq("2025-03-20","1403/12/30",false);
        eq("2025-03-21","1404/01/01",false);
        eq("2026-03-20","1404/12/29",false);
        eq("2026-10-17T17:30:00.000Z","1405/07/25",false);
        eq("","",false);
        reject("1404/12/30");
        reject("1405/12/30");
        reject("1405/07/31");
        reject("1405/13/01");
        reject("1405/00/01");
        reject("1405/01/00");
        for(int jy=1390;jy<=1420;jy++){
            for(int jm=1;jm<=12;jm++){
                for(int jd=1;jd<=31;jd++){
                    String jal=String.format(java.util.Locale.ROOT,"%04d/%02d/%02d",jy,jm,jd);
                    String iso;
                    try { iso=JalaliDates.toGregorian(jal); }
                    catch(IllegalArgumentException e) { continue; }
                    String roundTrip=JalaliDates.fromGregorian(iso);
                    if(!jal.equals(roundTrip)) throw new AssertionError(jal+" -> "+iso+" -> "+roundTrip);
                    checks++;
                }
            }
        }
        System.out.println("JalaliDates PASS "+checks+" checks");
    }
}
