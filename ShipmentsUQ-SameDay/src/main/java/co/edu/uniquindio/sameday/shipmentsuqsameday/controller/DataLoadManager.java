package co.edu.uniquindio.sameday.shipmentsuqsameday.controller;

import co.edu.uniquindio.sameday.shipmentsuqsameday.model.Deliverer;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.User;
import co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto.PaginatedDataDTO;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DataLoadManager {
    
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    public static <T> void loadDataAsync(TableView<T> tableView, List<T> data, int pageIndex, int pageSize) {
        Task<PaginatedDataDTO<T>> task = new Task<>() {
            @Override
            protected PaginatedDataDTO<T> call() {
                int fromIndex = pageIndex * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, data.size());
                
                List<T> pageData = data.subList(fromIndex, toIndex);
                int totalPages = (int) Math.ceil((double) data.size() / pageSize);
                
                return new PaginatedDataDTO<>(
                    pageData,
                    totalPages,
                    data.size(),
                    pageIndex
                );
            }
        };
        
        task.setOnSucceeded(e -> {
            PaginatedDataDTO<T> result = task.getValue();
            tableView.getItems().setAll(result.getItems());
        });
        
        new Thread(task).start();
    }
    
    public static CompletableFuture<PaginatedDataDTO<Object>> searchData(
            String searchTerm, 
            List<User> users, 
            List<Deliverer> deliverers, 
            int pageIndex, 
            int pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            List<Object> filteredData = users.stream()
                .filter(user -> matchesSearch(user, searchTerm))
                .collect(Collectors.toList());
            
            filteredData.addAll(
                deliverers.stream()
                    .filter(deliverer -> matchesSearch(deliverer, searchTerm))
                    .collect(Collectors.toList())
            );
            
            int fromIndex = pageIndex * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, filteredData.size());
            
            return new PaginatedDataDTO<>(
                filteredData.subList(fromIndex, toIndex),
                (int) Math.ceil((double) filteredData.size() / pageSize),
                filteredData.size(),
                pageIndex
            );
        });
    }
    
    private static boolean matchesSearch(User user, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String search = searchTerm.toLowerCase();
        return user.getName().toLowerCase().contains(search) ||
               user.getEmail().toLowerCase().contains(search) ||
               user.getPhone().toLowerCase().contains(search);
    }
    
    private static boolean matchesSearch(Deliverer deliverer, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String search = searchTerm.toLowerCase();
        return deliverer.getName().toLowerCase().contains(search) ||
               deliverer.getDocument().toLowerCase().contains(search) ||
               deliverer.getPhone().toLowerCase().contains(search);
    }

    public static void loadDataAsync(TableView<Object> tbl_data, ObservableList<?> data, int currentPage,
            int pageSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadDataAsync'");
    }
}