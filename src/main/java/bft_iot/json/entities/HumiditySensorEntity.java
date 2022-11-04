package bft_iot.json.entities;

public class HumiditySensorEntity extends FiwareCreateEntity {

    public Location location;
    public Temperature temperature;
    public Humidity humidity;

    public HumiditySensorEntity() {
        this.location = new Location();
        this.temperature = new Temperature();
        this.humidity = new Humidity();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }

    public String toString() {
        return String.format("{'id':'%s', 'type':'%s', 'location':%s, 'temperature':%s, 'humidity':%s}", id, type, location == null ? "null" : location.toString(), temperature == null ? "null" : temperature.toString(), humidity == null ? "null" : humidity.toString());
    }

    @Override
    public boolean compareTo(FiwareEntity other, BFTIoTConfig config) {
        float this_temp = this.temperature.value;
        float other_temp = ((HumiditySensorEntity)other).temperature.value;
        float temp_dif = Math.abs(this_temp -other_temp);

        float this_humid = this.humidity.value;
        float other_humid = ((HumiditySensorEntity)other).humidity.value;
        float humid_dif = Math.abs(this_humid -other_humid);

        return temp_dif <= config.temp_threshold && humid_dif <= config.humid_threshold;
    }

    public class Temperature {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }

        public void setTemperature(float newValue) {
            this.value = newValue;
        }
    }

    public class Humidity {
        public float value;
        public String type;

        public String toString() {
            return String.format("{'value':%f, 'type':'%s'}", value, type);
        }

        public void setHumidity(float newValue) {
            this.value = newValue;
        }
    }

    public class Location {
        private String value;
        private String type;

        public String toString() {
            return String.format("{'value':'%s', 'type':'%s'}", value, type);
        }
    }

}
