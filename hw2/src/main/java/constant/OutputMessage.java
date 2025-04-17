package constant;

public class OutputMessage {
  public static String SUCCESS = "Program type checked successfully";
  public static String ERROR = "Type error";

  public static void outputSuccess() {
    System.out.println(SUCCESS);
  }

  public static void outputErrorAndExit() {
    System.out.println(ERROR);
    System.exit(1);
  }
}
