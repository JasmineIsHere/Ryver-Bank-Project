package ryver.app.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class jsonDoubleSerializer extends JsonSerializer<Double> {
    public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException{
        if (null == value) {
            // Write the word 'null' if there's no value available
            jsonGenerator.writeNumber("0.00");
        } else {
            jsonGenerator.writeNumber(String.format("%.2f",(Math.round(value * 100) / 100.0)));
        }
    }
}