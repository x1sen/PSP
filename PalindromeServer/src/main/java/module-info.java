module com.example.palindromeserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.palindromeserver to javafx.fxml;
    exports com.example.palindromeserver;
}