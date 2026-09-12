package com.ecommerce.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.mybatis.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_dict_item")
public class DictItem extends BaseEntity {

    private String typeCode;
    private String itemValue;
    private String itemLabel;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
