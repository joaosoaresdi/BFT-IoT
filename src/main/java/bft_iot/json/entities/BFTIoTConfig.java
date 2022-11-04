package bft_iot.json.entities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class BFTIoTConfig {
    public int N;
    public int f;
    public double ratio_threshold;
    public double temp_threshold;
    public double humid_threshold;
    public String brokerHostname;
    public int brokerPort;

    private BFTIoTConfig(int N, int f, double ratio_threshold, double temp_threshold, double humid_threshold, String brokerHostname, int brokerPort){
        System.out.println(String.format("Starting BFT IoT Configuration (N, f, ratio_threshold, temp_threshold, humid_threshold, brokerHostname, brokerPort) = (%d, %d, %f, %f, %f, %s, %d)", N, f, ratio_threshold, temp_threshold, humid_threshold, brokerHostname, brokerPort));
        this.N = N;
        this.f = f;
        this.ratio_threshold = ratio_threshold;
        this.temp_threshold = temp_threshold;
        this.humid_threshold = humid_threshold;
        this.brokerHostname = brokerHostname;
        this.brokerPort = brokerPort;
    }

    public static BFTIoTConfig loadConfiguration(int procId) {
        try {
            String sep = System.getProperty("file.separator");
            InputStream input = new FileInputStream("config"+sep+"bft_iot.config");
            Properties properties = new Properties();
            properties.load(input);
            int n = Integer.parseInt(properties.getProperty("BFT-IoT.N"));
            int f = Integer.parseInt(properties.getProperty("BFT-IoT.f"));
            double ratio_threshold = Double.parseDouble(properties.getProperty("BFT-IoT.ratio_threshold"));
            double temp_threshold = Double.parseDouble(properties.getProperty("BFT-IoT.temp_threshold"));
            double humid_threshold = Double.parseDouble(properties.getProperty("BFT-IoT.humid_threshold"));
            String brokerHostname = (properties.getProperty("BFT-IoT.brokerHostname").split(",")[procId]);
            int brokerPort = Integer.parseInt(properties.getProperty("BFT-IoT.brokerPort"));
            return new BFTIoTConfig(n, f, ratio_threshold, temp_threshold, humid_threshold, brokerHostname, brokerPort);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return new BFTIoTConfig(4, 1, 0.5, 5.0, 20.0,"orion", 1026);
    }
}
