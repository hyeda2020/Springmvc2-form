package hello.typeconverter.formatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.Formatter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Slf4j
public class MyNumberFormatter implements Formatter<Number> {

    /**
     * "1000" -> 1000L
     */
    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        log.info("text={}, locale={}", text, locale);
        return NumberFormat.getInstance(locale).parse(text);
    }

    /**
     * 1000L -> "1000"
     */
    @Override
    public String print(Number number, Locale locale) {
        log.info("object={}, locale={}", number, locale);
        return NumberFormat.getInstance(locale).format(number);
    }
}
