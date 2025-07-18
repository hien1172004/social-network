package backend.example.mxh.DTO.response;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResponse<T>  {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private T items;
}
