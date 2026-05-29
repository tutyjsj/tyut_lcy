package com.ruoyi.tuyt.business.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.tuyt.business.assessment.entity.*;
import com.ruoyi.tuyt.business.assessment.mapper.AssessResultMapper;
import com.ruoyi.tuyt.business.assessment.service.IAssessResultService;
import com.ruoyi.tuyt.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssessResultServiceImpl extends ServiceImpl<AssessResultMapper, AssessResult> implements IAssessResultService {

    @Override
    public PageResult<AssessResult> queryPage(String assessPeriod, String month, String gridName, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AssessResult> wrapper = new LambdaQueryWrapper<>();
        if (assessPeriod != null && !assessPeriod.isEmpty()) {
            wrapper.eq(AssessResult::getAssessPeriod, assessPeriod);
        }
        // 支持按月份和网格名称查询（兼容前端 AssessmentQuery 页面）
        if (month != null && !month.isEmpty()) {
            wrapper.like(AssessResult::getAssessPeriod, month);
        }
        if (gridName != null && !gridName.isEmpty()) {
            wrapper.like(AssessResult::getGridName, gridName);
        }
        wrapper.orderByDesc(AssessResult::getScore);
        Page<AssessResult> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 执行考评：基于网格数据生成考评结果
     */
    @Override
    @Transactional
    public void generateResults(String type) {
        // 清除该周期的旧结果
        String period = resolvePeriod(type);
        remove(new LambdaQueryWrapper<AssessResult>().eq(AssessResult::getAssessPeriod, period));

        // 模拟生成网格考评结果
        List<AssessResult> results = buildMockResults(period);
        saveBatch(results);
    }

    private String resolvePeriod(String type) {
        if (type == null || type.isEmpty()) return "月度";
        return switch (type) {
            case "季度", "QUARTER" -> "季度";
            case "年度", "YEAR" -> "年度";
            default -> "月度";
        };
    }

    private List<AssessResult> buildMockResults(String period) {
        SecureRandom rng = new SecureRandom();
        // 网格名称列表（与系统中的网格数据对齐）
        String[] gridNames = {
                "尖草坪区网格", "万柏林区网格", "平城区网格",
                "城区网格", "潞州区网格", "小店区网格",
                "杏花岭区网格", "迎泽区网格", "云冈区网格",
                "矿区网格", "上党区网格", "榆次区网格",
                "太谷区网格", "尧都区网格", "侯马市网格"
        };
        long[] gridIds = {102L, 103L, 201L, 301L, 401L, 101L,
                         104L, 105L, 202L, 302L, 402L, 501L,
                         502L, 601L, 602L};

        List<AssessResult> list = new ArrayList<>();
        for (int i = 0; i < gridNames.length; i++) {
            double rawScore = 55 + rng.nextDouble() * 40; // 55~95
            BigDecimal score = BigDecimal.valueOf(Math.round(rawScore * 10) / 10.0);

            String level;
            if (score.compareTo(new BigDecimal("90")) >= 0) level = "A";
            else if (score.compareTo(new BigDecimal("80")) >= 0) level = "B";
            else if (score.compareTo(new BigDecimal("70")) >= 0) level = "C";
            else level = "D";

            AssessResult r = new AssessResult();
            r.setGridId(gridIds[i]);
            r.setGridName(gridNames[i]);
            r.setRuleId(1L);
            r.setScore(score);
            r.setLevel(level);
            r.setAssessPeriod(period);
            // 根据评分生成关联指标
            double base = score.doubleValue();
            r.setResponseRate(BigDecimal.valueOf(Math.round((base + rng.nextDouble() * 5) * 10) / 10.0));
            r.setDisposalRate(BigDecimal.valueOf(Math.round((base - 2 + rng.nextDouble() * 6) * 10) / 10.0));
            r.setCompleteRate(BigDecimal.valueOf(Math.round((base - 1 + rng.nextDouble() * 4) * 10) / 10.0));
            list.add(r);
        }
        return list;
    }
}
