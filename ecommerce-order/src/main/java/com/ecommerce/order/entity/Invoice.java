package com.ecommerce.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_invoice")
public class Invoice extends BaseEntity {

    private Long orderId;
    private Long userId;
    /** 1=个人, 2=企业 */
    private Integer type;
    /** 发票抬头 */
    private String title;
    /** 税号(企业必填) */
    private String taxNo;
    private BigDecimal amount;
    /** 接收邮箱 */
    private String email;
    /** 联系电话 */
    private String phone;
    /** 0=待开票, 1=已开票, 2=已拒绝 */
    private Integer status;
    /** 发票号码 */
    private String invoiceNo;
    /** 电子发票PDF地址 */
    private String invoiceUrl;
    private String remark;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime issueTime;
}
