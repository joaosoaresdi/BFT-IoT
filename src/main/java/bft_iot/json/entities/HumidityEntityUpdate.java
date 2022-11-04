package bft_iot.json.entities;

public class HumidityEntityUpdate extends FiwareEntity {
    public Temperature temperature;
    public Humidity humidity;

    public HumidityEntityUpdate() {
        this.temperature = new Temperature();
        this.humidity = new Humidity();
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public String toString() {
        return String.format("{'temperature':%s, 'humidity':%s}", temperature == null ? "null" : temperature.toString(), humidity == null ? "null" : humidity.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        float this_temp = this.temperature.value;
        float other_temp = ((HumidityEntityUpdate)other).temperature.value;
        float temp_dif = Math.abs(this_temp -other_temp);

        float this_humid = this.humidity.value;
        float other_humid = ((HumidityEntityUpdate)other).humidity.value;
        float humid_dif = Math.abs(this_humid -other_humid);

        return temp_dif <= config.temp_threshold && humid_dif <= config.humid_threshold;
    }

    public class Temperature {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }
    }

    public class Humidity {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }
    }
}
