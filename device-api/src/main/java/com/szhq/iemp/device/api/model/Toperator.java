package com.szhq.iemp.device.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.szhq.iemp.common.model.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

/**
 * 运营公司
 */
@Entity(name = "t_operator")
@Data
@DynamicUpdate
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Toperator extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(20) COMMENT '名称'")
    private String name;

    @Column(columnDefinition = "varchar(20) COMMENT '地址'")
    private String address;

    @Column(columnDefinition = "varchar(20) COMMENT '经度'")
    private String lon;

    @Column(columnDefinition = "varchar(20) COMMENT '纬度'")
    private String lat;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(columnDefinition = "INT COMMENT '父运营公司Id'", name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    private Toperator parent;

    @Column(columnDefinition = "INT COMMENT '仓库Id'")
    private Integer storehouseId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinColumn(columnDefinition = "INT COMMENT '归属地id'", nullable = false, name = "region_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    private TaddressRegion region;

    //是否打开webSocket开关（0:关闭,1:打开）
    private Integer flag;
    /**
     * 车牌前缀
     */
    @Transient
    private List<Integer> platenoPrefixIds;

    @Transient
    private Integer deviceCount;

    @Transient
    private Integer activeCount;

    @Transient
    private Integer unActiveCount;

    @Transient
    private Integer installedCount;

    @Transient
    private Integer saleCount;

}
