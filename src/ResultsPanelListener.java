import java.util.List;

public interface ResultsPanelListener {

    void onBackButtonPressed();

    void onProceedToDetails(List<MenuItem> selectedItems);
}
