package co.edu.uniquindio.sameday.shipmentsuqsameday.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDataDTO<T> {
    private List<T> items;
    private int totalPages;
    private long totalItems;
    private int currentPage;
}