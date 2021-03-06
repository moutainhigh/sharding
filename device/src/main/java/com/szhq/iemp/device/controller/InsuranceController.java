package com.szhq.iemp.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.szhq.iemp.common.constant.ResultConstant;
import com.szhq.iemp.common.exception.NbiotException;
import com.szhq.iemp.common.util.DencryptTokenUtil;
import com.szhq.iemp.common.vo.MyPage;
import com.szhq.iemp.common.vo.Result;
import com.szhq.iemp.device.api.model.*;
import com.szhq.iemp.device.api.service.*;
import com.szhq.iemp.device.api.vo.DeviceVo;
import com.szhq.iemp.device.api.vo.PolicyInfo;
import com.szhq.iemp.device.api.vo.PolicyName;
import com.szhq.iemp.device.api.vo.query.DeviceQuery;
import com.szhq.iemp.device.api.vo.query.PolicyQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Api(description = "保险模块")
@RestController
@RequestMapping("/insurance")
@Slf4j
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;
    @Autowired
    private PolicyInfoService policyInfoService;
    @Autowired
    private UserInsuranceService userInsuranceService;
    @Autowired
    private DeviceInventoryService deviceInventoryService;
    @Autowired
    private DeviceStoreHouseService storeHouseService;

    @ApiOperation(value = "列表", notes = "列表")
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Result search(@RequestParam(value = "offset") Integer offset,
                         @RequestParam(value = "pagesize") Integer limit,
                         @RequestParam(required = false, value = "sort") String sort,
                         @RequestParam(required = false, value = "order") String order,
                         @RequestBody(required = false) PolicyQuery query) {
        Map<String, Object> result = new HashMap<>();
        log.info("policy-query:" + JSONObject.toJSONString(query));
        MyPage<TpolicyInfo> list = policyInfoService.findAllByCriteria(offset, limit, sort, order, query);
        result.put("policys", list.getContent());
        result.put("total", list.getTotal());
        return new Result(ResultConstant.SUCCESS, result);
    }

    @ApiOperation(value = "添加保单", notes = "添加保单")
    @RequestMapping(value = "/addPolicyInfo", method = RequestMethod.POST)
    public Result addPolicy(@Valid @RequestBody PolicyInfo entity) {
        TpolicyInfo policyInfo = policyInfoService.add(entity);
        if(policyInfo != null){
            return new Result(ResultConstant.SUCCESS, policyInfo);
        }
        return new Result(ResultConstant.FAILED, "");
    }

    @ApiOperation(value = "根据用户Id查询保单信息", notes = "根据用户Id查询保单信息")
    @GetMapping("/policyInfoByUserId")
    public Result policyInfoByUserId(@RequestParam(value = "userId")String userId) {
        List<TpolicyInfo> list = new ArrayList<>();
        List<TpolicyInfo> policyInfos = policyInfoService.getPolicyInfoByUserId(userId);
        if(policyInfos != null && !policyInfos.isEmpty()){
            for(TpolicyInfo policyInfo : policyInfos){
                List<TuserInsurance> userInsurances = userInsuranceService.findByPolicyId(policyInfo.getId());
                if(userInsurances != null && !userInsurances.isEmpty()){
                    List<Integer> insurancesIds = userInsurances.stream().map(TuserInsurance::getInsuranceId).collect(Collectors.toList());
                    log.info("insurancesIds:" + JSONObject.toJSONString(insurancesIds));
                    if(!insurancesIds.isEmpty()){
                        List<Tinsurance> insurances = insuranceService.findByIdIn(insurancesIds);
                        policyInfo.setInsuranceList(insurances);
                        list.add(policyInfo);
                    }
                }else{
                    list.add(policyInfo);
                }
            }
        }
        return new Result(ResultConstant.SUCCESS, list);
    }

    @ApiOperation(value = "根据车牌查找是否关联保险", notes = "根据车牌查找是否关联保险(true:已关联)")
    @RequestMapping(value = "/findByPlateNo", method = RequestMethod.GET)
    public Result findByPlateNo(@RequestParam(value = "plateNo")String plateNo) {
        TpolicyInfo policyInfo = policyInfoService.findByPlateNo(plateNo);
        if(policyInfo != null){
            return new Result(ResultConstant.SUCCESS, true);
        }
        return new Result(ResultConstant.SUCCESS, false);
    }

    @ApiOperation(value = "根据保单Id查询保单详情", notes = "根据保单Id查询保单详情")
    @RequestMapping(value = "/findByPolicyId", method = RequestMethod.GET)
    public Result findByPolicyId(@RequestParam(value = "policyId")Long policyId) {
        TpolicyInfo policyInfo = policyInfoService.getPolicyById(policyId);
        if(policyInfo == null){
            return new Result(ResultConstant.SUCCESS, "");
        }
        List<TuserInsurance> userInsurances = userInsuranceService.findByPolicyId(policyId);
        if(userInsurances != null && !userInsurances.isEmpty()){
            List<Integer> ids = userInsurances.stream().map(TuserInsurance::getInsuranceId).collect(Collectors.toList());
            List<Tinsurance> insurances = insuranceService.findByIdIn(ids);
            policyInfo.setInsuranceList(insurances);
        }
        return new Result(ResultConstant.SUCCESS, policyInfo);
    }

    @ApiOperation(value = "添加保险", notes = "添加保险")
    @RequestMapping(value = "/addInsurance", method = RequestMethod.POST)
    public Result addInsurance(@Valid @RequestBody Tinsurance entity) {
        String name = insuranceService.getNameByCode(entity.getNameCode());
        String type = insuranceService.getTypeByCode(entity.getTypeCode());
        if(StringUtils.isEmpty(name) || StringUtils.isEmpty(type)){
            log.error("name or type is not found.");
            throw new NbiotException(400, "");
        }
        entity.setName(name);
        entity.setType(type);
        Tinsurance tinsurance = insuranceService.add(entity);
        if(tinsurance != null){
            return new Result(ResultConstant.SUCCESS, tinsurance);
        }
        return new Result(ResultConstant.FAILED, "");
    }

    @ApiOperation(value = "全部查询保险列表", notes = "全部查询保险列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list() {
        List<Tinsurance> insurances = insuranceService.list();
        return new Result(ResultConstant.SUCCESS, insurances);
    }

    @ApiOperation(value = "根据保险公司Id查询全部查询保险列表", notes = "根据保险公司Id查询全部查询保险列表")
    @RequestMapping(value = "/listByPolicyCode", method = RequestMethod.GET)
    public Result listByPolicyCode(@RequestParam(value = "code")Integer id) {
        List<Tinsurance> insurances = insuranceService.listByPolicyCode(id);
        return new Result(ResultConstant.SUCCESS, insurances);
    }

    @ApiOperation(value = "删除保险", notes = "删除保险")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam("id") Integer id) {
        Integer i = insuranceService.delete(id);
        return new Result(ResultConstant.SUCCESS, i);
    }

    @ApiOperation(value = "导出保单", notes = "根据时间导出保单数据")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void excel(@RequestParam("startTime") Date startTime,
                      @RequestParam("endTime") Date endTime, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PolicyQuery query = new PolicyQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        List<Integer> operatorIds = DencryptTokenUtil.getOperatorIds(request);
        if (operatorIds != null && operatorIds.get(0) != 0) {
            query.setOperatorIdList(operatorIds);
        }
        log.info("export policy query:" + JSONObject.toJSONString(query));
        policyInfoService.exportExcel(response, query);
    }

    @ApiOperation(value = "查询全部保险名称", notes = "查询全部保险名称")
    @RequestMapping(value = "/getAllNames", method = RequestMethod.GET)
    public Result getAllNames() {
        List<PolicyName> map = insuranceService.getAllNames();
        return new Result(ResultConstant.SUCCESS, map);
    }

    @ApiOperation(value = "查询全部保险类型", notes = "查询全部保险类型")
    @RequestMapping(value = "/getAllTypes", method = RequestMethod.GET)
    public Result getAllTypes() {
        Map<Integer, String> map = insuranceService.getAllTypes();
        return new Result(ResultConstant.SUCCESS, map);
    }

    @ApiOperation(value = "分页查询保险列表", notes = "分页查询保险列表")
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Result page(@RequestParam(value = "page") int page,
                       @RequestParam(value = "size") int size) {
        Page<Tinsurance> insurances = insuranceService.page(page,size);
        return new Result(ResultConstant.SUCCESS, insurances);
    }

    @ApiOperation(value = "初始化保单信息", notes = "初始化保单信息")
    @RequestMapping(value = "/initializePolicy", method = RequestMethod.POST)
    public Result initializePolicy(@RequestParam(value = "imei") String imei) {
        Long count = policyInfoService.initializePolicy(imei);
        return new Result(ResultConstant.SUCCESS, count);
    }

    @ApiOperation(value = "新增保单信息（之前无保单属性）", notes = "新增保单信息")
    @RequestMapping(value = "/addNewPolicy", method = RequestMethod.POST)
    public Result addNewPolicy(@RequestBody TpolicyInfo entity) {
        if(StringUtils.isEmpty(entity.getImei()) || entity.getNameCode() == null || StringUtils.isEmpty(entity.getName())){
            log.error("wrong parameter.entity:{}", JSONObject.toJSONString(entity));
            throw new NbiotException(400, "参数错误");
        }
        Long count = policyInfoService.addNewPolicy(entity);
        return new Result(ResultConstant.SUCCESS, count);
    }

    @ApiOperation(value = "310所有设备列表", notes = "310所有设备列表")
    @RequestMapping(value = "/310devices", method = RequestMethod.POST)
    public Result devices(@RequestParam(value = "offset") Integer offset,
                          @RequestParam(value = "pagesize") Integer limit,
                          @RequestBody(required = false) DeviceQuery query) {
        log.info("310devices query:{}", JSONObject.toJSONString(query));
        Map<String, Object> result = new HashMap<>();
        MyPage<DeviceVo> list = policyInfoService.devices(offset,limit, query);
        result.put("policys", list.getContent());
        result.put("total", list.getTotal());
        return new Result(ResultConstant.SUCCESS, result);
    }

    @ApiOperation(value = "根据imeis查询保单须知", notes = "根据imeis查询保单须知")
    @RequestMapping(value = "/findPolicyInstructionByImeis", method = RequestMethod.POST)
    public Result findByPolicyId(@RequestBody List<String> imeis) {
        List<String> urls = new ArrayList<>();
        if(imeis == null || imeis.isEmpty()){
            return new Result(ResultConstant.SUCCESS, urls);
        }
        List<TdeviceInventory> devices = deviceInventoryService.findByImeiIn(imeis);
        if(devices != null && !devices.isEmpty()){
            List<Integer> storehouseIds = devices.stream().map(TdeviceInventory::getStorehouseId).distinct().collect(Collectors.toList());
            List<TdeviceStoreHouse> storeHouses = storeHouseService.findByIds(storehouseIds);
            if(storeHouses != null && !storeHouses.isEmpty()){
                urls = storeHouses.stream().map(TdeviceStoreHouse::getPolicyInstructionUrl).collect(Collectors.toList());
            }
        }
        return new Result(ResultConstant.SUCCESS, urls);
    }

    @ApiOperation(value = "补充未填保单（测试）", notes = "补充未填保单（测试）")
    @RequestMapping(value = "/setPolicy", method = RequestMethod.GET)
    public Result setPolicy() {
        Integer count = policyInfoService.getLostPolicys();
        return new Result(ResultConstant.SUCCESS, count);
    }

}
