package finalproject.emag.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;

public class GetDate {

    public static LocalDate getDate(String inputDate) throws ParseException {
        LocalDate date = null;
        if(inputDate!=null) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            format.setLenient(false);
            date = format.parse(inputDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return date;
    }
}
