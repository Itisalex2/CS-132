package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class InputReader {
  public static String readAll() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    StringBuilder inputBuilder = new StringBuilder();
    String line;

    while ((line = reader.readLine()) != null) {
      inputBuilder.append(line).append("\n");
    }

    return inputBuilder.toString();
  }
}
