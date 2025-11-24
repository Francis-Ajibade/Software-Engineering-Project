import java.util.*;

class Sally {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();

        if (a > 1000000 || b > 1000000) {
            System.out.println("ERROR"); // Cannot handle large numbers
        } else {
            System.out.println(a + b);
        }
    }
}
