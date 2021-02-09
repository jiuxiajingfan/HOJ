package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/18 18:16
 * @Description:
 */

@Data
@Accessors(chain = true)
public class OIContestRankVo {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "提交总得分")
    private Integer totalScore;

    @ApiModelProperty(value = "OI的题对应提交得分")
    private HashMap<String, Integer> submissionInfo;
}