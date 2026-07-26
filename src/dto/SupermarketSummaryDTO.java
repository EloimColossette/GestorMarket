package dto;

import java.math.BigDecimal;

public class SupermarketSummaryDTO {

    private Integer supermarketId;
    private String supermarketName;
    private BigDecimal total;

    public SupermarketSummaryDTO() {}

    public SupermarketSummaryDTO(Integer supermarketId, String supermarketName, BigDecimal total) {
        this.supermarketId = supermarketId;
        this.supermarketName = supermarketName;
        this.total = total;
    }

    public Integer getSupermarketId() { return supermarketId; }
    public void setSupermarketId(Integer supermarketId) { this.supermarketId = supermarketId; }

    public String getSupermarketName() { return supermarketName; }
    public void setSupermarketName(String supermarketName) { this.supermarketName = supermarketName; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}