package won.ecommerce.repository.dto.search.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class SellerInfoDto {
    private String sellerNickname;
    private String sellerEmail;
    private String sellerPNum;

    @QueryProjection
    public SellerInfoDto(String sellerNickname, String sellerEmail, String sellerPNum) {
        this.sellerNickname = sellerNickname;
        this.sellerEmail = sellerEmail;
        this.sellerPNum = sellerPNum;
    }
}
