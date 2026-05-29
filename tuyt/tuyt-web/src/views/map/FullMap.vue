<template>
  <div class="fullmap-wrapper">
    <!-- 顶部搜索栏 -->
    <div class="map-search-bar">
      <div class="search-left">
        <el-input
          v-model="keyword"
          placeholder="搜索企业名称、地址…"
          class="search-input"
          clearable
          @keyup.enter="doSearch"
          @clear="clearSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="small" @click="doSearch">搜索</el-button>
        <el-button size="small" :type="showAdvanced ? 'warning' : ''" @click="showAdvanced = !showAdvanced">
          <el-icon><MoreFilled /></el-icon> 高级
        </el-button>
      </div>
      <div class="search-right">
        <el-input
          v-model="coordInput"
          placeholder="坐标反解: 经度,纬度"
          class="coord-input"
          clearable
          @keyup.enter="doGeocode"
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
        </el-input>
        <el-button size="small" @click="doGeocode">反解</el-button>
      </div>
    </div>

    <!-- 高级搜索面板 -->
    <div class="advanced-panel" v-show="showAdvanced">
      <el-tabs v-model="advancedTab" size="small">
        <el-tab-pane label="地名库查询" name="placename">
          <div class="adv-row">
            <el-input v-model="placeKeyword" placeholder="输入地名、POI、路段…" size="small" style="width:200px" @keyup.enter="doPlaceSearch" />
            <el-select v-model="placeCity" size="small" style="width:100px">
              <el-option label="太原市" value="太原" />
              <el-option label="大同市" value="大同" />
              <el-option label="北京市" value="北京" />
              <el-option label="全国" value="全国" />
            </el-select>
            <el-select v-model="placeType" size="small" style="width:120px" clearable placeholder="POI类型">
              <el-option label="商务住宅" value="120000" />
              <el-option label="工厂/企业" value="170000" />
              <el-option label="医疗保健" value="090000" />
              <el-option label="学校" value="141200" />
              <el-option label="政府机构" value="130000" />
              <el-option label="公园广场" value="110000" />
              <el-option label="全部类型" value="" />
            </el-select>
            <el-button type="primary" size="small" @click="doPlaceSearch">地名库检索</el-button>
            <el-button size="small" @click="clearPlaceResults" v-if="placeResults.length">清除({{ placeResults.length }})</el-button>
          </div>
          <div class="adv-results" v-if="placeResults.length">
            <div class="adv-tags">
              <el-tag v-for="(p, i) in placeResults.slice(0, 30)" :key="i"
                size="small" style="cursor:pointer;margin:2px" @click="locatePlace(p)">
                {{ p.name }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="区划查询" name="district">
          <div class="adv-row">
            <el-input v-model="districtKeyword" placeholder="输入区县名称，如: 迎泽区" size="small" style="width:180px" @keyup.enter="searchDistrict" />
            <el-button type="primary" size="small" @click="searchDistrict">查询区划</el-button>
            <el-button size="small" @click="clearDistrict">清除区划</el-button>
          </div>
          <div class="adv-results" v-if="districtList.length">
            <div class="adv-tags">
              <el-tag v-for="(d, i) in districtList" :key="i" size="small"
                style="cursor:pointer;margin:2px" @click="showDistrictOnMap(d)">
                {{ d.name }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="图层编号查询" name="layerquery">
          <div class="adv-row">
            <el-select v-model="layerQueryType" size="small" style="width:130px" placeholder="选择图层">
              <el-option label="污染源点位" value="pollution" />
              <el-option label="周围敏感点" value="sensitive" />
              <el-option label="感知设备" value="device" />
              <el-option label="人员位置" value="personnel" />
            </el-select>
            <el-input v-model="layerQueryKeyword" placeholder="输入名称/编号关键词" size="small" style="width:180px" @keyup.enter="doLayerSearch" />
            <el-button type="primary" size="small" @click="doLayerSearch">图层检索</el-button>
          </div>
          <el-table :data="layerQueryResults" size="small" max-height="200" style="width:100%" v-if="layerQueryResults.length">
            <el-table-column prop="name" label="名称" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="80" />
            <el-table-column prop="status" label="状态" width="60">
              <template #default="{ row }">
                <span :style="{color: row.status === '在线' ? '#67C23A' : '#909399'}">{{ row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="50">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="locateToLayerResult(row)">定位</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="layerQuerySearched && layerQueryResults.length === 0" style="padding:8px;color:#909399;font-size:12px">未找到匹配结果</div>
        </el-tab-pane>
        <el-tab-pane label="业务库关联" name="bizlink">
          <div class="adv-row">
            <el-select v-model="bizLinkType" size="small" style="width:120px" placeholder="业务类型">
              <el-option label="环境问题" value="problem" />
              <el-option label="巡查任务" value="task" />
              <el-option label="考评记录" value="assessment" />
              <el-option label="网格信息" value="grid" />
            </el-select>
            <el-input v-model="bizLinkKeyword" placeholder="业务数据关键词" size="small" style="width:180px" @keyup.enter="doBizLinkSearch" />
            <el-button type="primary" size="small" @click="doBizLinkSearch">关联检索</el-button>
          </div>
          <div style="padding:6px 0;color:#909399;font-size:12px">
            将GIS空间数据与系统业务数据库进行关联查询，实现"以图查数、以数查图"
          </div>
          <el-table :data="bizLinkResults" size="small" max-height="200" style="width:100%" v-if="bizLinkResults.length">
            <el-table-column prop="name" label="名称" show-overflow-tooltip />
            <el-table-column prop="bizType" label="业务类型" width="80" />
            <el-table-column prop="location" label="位置" width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.lng">{{ row.lng.toFixed(4) }}, {{ row.lat.toFixed(4) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="mapBizLink(row)">地图定位</el-button>
                <el-button v-if="row.bizType === '企业'" link type="success" size="small" @click="openEnterpriseDetail(row.id)">档案</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="bizLinkSearched && bizLinkResults.length === 0" style="padding:8px;color:#909399;font-size:12px">未找到关联记录</div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="map-body">
      <!-- 左侧图层面板 -->
      <div class="layer-panel">
        <div class="panel-header">
          <el-icon class="panel-icon"><Grid /></el-icon>
          <span class="panel-title">专题图层</span>
          <span class="layer-count-badge">{{ totalVisibleLayers }}</span>
        </div>
        <div class="layer-list">
          <div class="layer-item" v-for="layer in layerList" :key="layer.key"
            :class="{ 'layer-item-active': layer.visible }">
            <div class="layer-item-left">
              <div class="layer-dot" :style="{background: layer.color}"></div>
              <span class="layer-label">{{ layer.label }}</span>
            </div>
            <div class="layer-item-right">
              <el-badge :value="layer.count" class="layer-badge" :max="999" v-if="layer.count > 0" />
              <el-switch v-model="layer.visible" size="small" @change="toggleLayer(layer.key)" />
            </div>
          </div>
        </div>
        <div class="layer-actions">
          <el-button size="small" text @click="toggleAllLayers(true)">全开</el-button>
          <el-button size="small" text @click="toggleAllLayers(false)">全关</el-button>
        </div>
        
        <!-- 图例 -->
        <div class="panel-divider"></div>
        <div class="panel-subtitle">图例</div>
        <div class="legend-list">
          <div class="legend-item" v-for="lg in legendList" :key="lg.label">
            <span class="legend-dot" :style="{background: lg.color}"></span>
            <span>{{ lg.label }}</span>
          </div>
        </div>
        
        <!-- 统计数据卡片 -->
        <div class="panel-divider"></div>
        <div class="panel-subtitle">概览统计</div>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value stat-blue">{{ mapStats.enterprises }}</span>
            <span class="stat-label">企业</span>
          </div>
          <div class="stat-item">
            <span class="stat-value stat-orange">{{ mapStats.sensitivePoints }}</span>
            <span class="stat-label">敏感点</span>
          </div>
          <div class="stat-item">
            <span class="stat-value stat-blue2">{{ mapStats.devices }}</span>
            <span class="stat-label">设备</span>
          </div>
          <div class="stat-item">
            <span class="stat-value stat-green">{{ mapStats.personnel }}</span>
            <span class="stat-label">人员</span>
          </div>
        </div>
      </div>

      <!-- 地图容器 -->
      <div id="full-map" class="map-container">
        <div v-if="mapLoading" class="map-loading-full">
          <div class="loading-spin"><el-icon class="is-loading" :size="48"><Loading /></el-icon></div>
          <span class="loading-text-full">地图加载中，请稍候…</span>
        </div>
      </div>

      <!-- 右侧工具条 -->
      <div class="tool-panel">
        <div class="tool-btn" :class="{active: activeTool === 'ranging'}" title="测距" @click="startRanging">
          <el-icon><Connection /></el-icon>
          <span>测距</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'area'}" title="测面积" @click="startAreaMeasure">
          <el-icon><Crop /></el-icon>
          <span>测面</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'rect'}" title="框选放大" @click="startRectSelect">
          <el-icon><Select /></el-icon>
          <span>框选</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'line'}" title="画直线" @click="startDraw('line')">
          <el-icon><Minus /></el-icon>
          <span>直线</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'polyline'}" title="画折线" @click="startDraw('polyline')">
          <el-icon><Link /></el-icon>
          <span>折线</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'polygon'}" title="画区域" @click="startDraw('polygon')">
          <el-icon><Aim /></el-icon>
          <span>区域</span>
        </div>
        <div class="tool-btn" :class="{active: activeTool === 'text'}" title="文字标注" @click="startDraw('marker')">
          <el-icon><Edit /></el-icon>
          <span>标注</span>
        </div>
        <div class="tool-divider"></div>
        <div class="tool-btn" :class="{active: activeTool === 'buffer'}" title="缓冲区查询" @click="startBufferQuery">
          <el-icon><CircleCheck /></el-icon>
          <span>缓冲区</span>
        </div>
        <div class="tool-btn" title="条件查询" @click="advQueryVisible = true">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </div>
        <div class="tool-btn" title="全景图" @click="panoramaVisible = true">
          <el-icon><Picture /></el-icon>
          <span>全景图</span>
        </div>
        <div class="tool-divider"></div>
        <div class="tool-btn" title="还原" @click="restoreMap">
          <el-icon><RefreshLeft /></el-icon>
          <span>还原</span>
        </div>
        <div class="tool-btn" title="清除标注" @click="clearDrawings">
          <el-icon><Delete /></el-icon>
          <span>清除</span>
        </div>
        <div class="tool-btn" title="全屏" @click="goFullScreen">
          <el-icon><FullScreen /></el-icon>
          <span>全屏</span>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="map-status">
      <span>鼠标坐标: {{ mouseCoord || '移动鼠标查看' }}</span>
      <span style="margin-left:16px">在线网格员: {{ personnelMarkers.length }} 人</span>
      <span style="margin-left:16px">企业标记: {{ enterpriseMarkers.length }} 个</span>
      <span v-if="geoEnabled" style="margin-left:16px;color:#67C23A">📍GPS: {{ geoCoord || '获取中…' }}</span>
      <span style="margin-left:auto;display:flex;align-items:center;gap:4px">
        <span style="font-size:11px;color:#909399">底图:</span>
        <el-button-group size="small">
          <el-button :type="baseMap === 'normal' ? 'primary' : ''" size="small" @click="switchBaseMap('normal')">标准</el-button>
          <el-button :type="baseMap === 'satellite' ? 'primary' : ''" size="small" @click="switchBaseMap('satellite')">卫星</el-button>
          <el-button :type="baseMap === 'view3d' ? 'primary' : ''" size="small" @click="switchBaseMap('view3d')">3D</el-button>
        </el-button-group>
      </span>
    </div>

    <!-- 周边查询弹窗 -->
    <el-dialog v-model="nearbyVisible" title="周边查询" width="450px" append-to-body>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-select v-model="nearbyType" placeholder="查询类型" style="width:140px">
          <el-option label="周边企业" value="enterprise" />
          <el-option label="周边敏感点" value="sensitive" />
          <el-option label="周边污染源" value="pollution" />
        </el-select>
        <el-input-number v-model="nearbyRadius" :min="100" :max="10000" :step="100" style="width:130px" />
        <span style="line-height:32px">米</span>
        <el-button type="primary" size="small" @click="doNearbySearch">查询</el-button>
      </div>
      <div>当前查询中心: {{ nearbyCenter ? nearbyCenter.join(', ') : '-' }}</div>
      <el-table :data="nearbyResults" size="small" max-height="300" style="margin-top:10px">
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column prop="distance" label="距离(m)" width="80">
          <template #default="{ row }">{{ row.distance ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="50">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="locateTo(row)">定位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 缓冲区查询弹窗 -->
    <el-dialog v-model="bufferVisible" title="缓冲区查询" width="480px" append-to-body>
      <div style="margin-bottom:10px;color:#606266;font-size:13px">
        <el-tag type="info" size="small">{{ bufferStep === 'pick' ? '请在地图上点击以确定缓冲区中心点' : '中心已选定，可设置半径查询' }}</el-tag>
      </div>
      <div v-if="bufferCenter" style="margin-bottom:10px">
        <span style="color:#909399">缓冲区中心:</span>
        <strong>{{ bufferCenter[0].toFixed(5) }}, {{ bufferCenter[1].toFixed(5) }}</strong>
      </div>
      <div style="display:flex;gap:8px;align-items:center;margin-bottom:12px">
        <span>缓冲区半径:</span>
        <el-input-number v-model="bufferRadius" :min="100" :max="20000" :step="100" style="width:140px" :disabled="!bufferCenter" />
        <span>米</span>
        <el-select v-model="bufferQueryType" style="width:130px" :disabled="!bufferCenter">
          <el-option label="企业" value="enterprise" />
          <el-option label="敏感点" value="sensitive" />
          <el-option label="污染源" value="pollution" />
          <el-option label="全部" value="all" />
        </el-select>
        <el-button type="primary" size="small" :disabled="!bufferCenter" @click="doBufferSearch">查询</el-button>
        <el-button size="small" @click="clearBuffer">清除</el-button>
      </div>
      <el-table :data="bufferResults" size="small" max-height="250">
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="70" />
        <el-table-column prop="distance" label="距离(m)" width="80">
          <template #default="{ row }">{{ row.distance ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="50">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="locateToBuffer(row)">定位</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="bufferResults.length" style="margin-top:8px;color:#909399;font-size:12px">
        缓冲区内共 {{ bufferResults.length }} 条记录
      </div>
    </el-dialog>

    <!-- 条件查询企业弹窗 -->
    <el-dialog v-model="advQueryVisible" title="条件查询企业" width="500px" append-to-body>
      <el-form :model="advQueryForm" label-width="80px" size="small">
        <el-form-item label="企业名称">
          <el-input v-model="advQueryForm.name" placeholder="输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="污染类型">
          <el-select v-model="advQueryForm.pollutionType" placeholder="选择污染类型" clearable style="width:100%">
            <el-option label="废气" value="废气" />
            <el-option label="废水" value="废水" />
            <el-option label="固废" value="固废" />
            <el-option label="噪声" value="噪声" />
            <el-option label="扬尘" value="扬尘" />
          </el-select>
        </el-form-item>
        <el-form-item label="监管等级">
          <el-select v-model="advQueryForm.superviseType" placeholder="选择监管等级" clearable style="width:100%">
            <el-option label="重点监管" value="重点监管" />
            <el-option label="一般监管" value="一般监管" />
            <el-option label="特殊监管" value="特殊监管" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在区域">
          <el-input v-model="advQueryForm.address" placeholder="输入地址关键词" clearable />
        </el-form-item>
        <el-form-item label="违法状态">
          <el-radio-group v-model="advQueryForm.violating">
            <el-radio :value="null">全部</el-radio>
            <el-radio :value="true">违法</el-radio>
            <el-radio :value="false">正常</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="advQueryVisible = false">取消</el-button>
        <el-button @click="resetAdvQuery">重置</el-button>
        <el-button type="primary" @click="doAdvQuery">查询</el-button>
      </template>
      <el-table v-if="advQueryResults.length" :data="advQueryResults" size="small" max-height="250" style="margin-top:10px">
        <el-table-column prop="enterpriseName" label="企业名称" show-overflow-tooltip />
        <el-table-column prop="pollutionType" label="污染类型" width="80" />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isViolating ? 'danger' : ''" size="small">{{ row.isViolating ? '违法' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="locateToEnterprise(row)">定位</el-button>
            <el-button link type="primary" size="small" @click="openEnterpriseDetail(row.id)">档案</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="advQuerySearched && !advQueryResults.length" style="color:#909399;font-size:13px;text-align:center;padding:16px">
        未找到匹配企业
      </div>
    </el-dialog>

    <!-- 全景图弹窗 -->
    <el-dialog v-model="panoramaVisible" title="全景图浏览" width="700px" append-to-body>
      <div style="text-align:center;color:#606266;padding:20px">
        <el-empty description="全景图需要接入腾讯街景/高德全景等服务">
          <template #image>
            <div style="font-size:64px;line-height:1">🖼️</div>
          </template>
        </el-empty>
        <div style="margin-top:12px;font-size:13px;color:#909399">
          <p>目前提供两种全景点位预览方式：</p>
          <div style="display:flex;gap:12px;justify-content:center;margin-top:12px">
            <el-button type="primary" @click="openTencentStreetView" :disabled="!panoramaPoint">
              <el-icon><Picture /></el-icon> 腾讯街景
            </el-button>
            <el-button type="warning" @click="openGaodePanorama" :disabled="!panoramaPoint">
              <el-icon><VideoCamera /></el-icon> 高德全景
            </el-button>
          </div>
          <p style="margin-top:12px;font-size:12px">
            当前全景坐标: {{ panoramaPoint ? panoramaPoint.join(', ') : '请先在地图上点击选取全景点位' }}
          </p>
          <el-button size="small" style="margin-top:6px" @click="startPickPanoramaPoint">
            <el-icon><Aim /></el-icon> 拾取全景坐标
          </el-button>
        </div>
        <div v-if="panoramaLink" style="margin-top:10px">
          <p style="color:#67C23A;font-size:13px">全景图链接已生成：</p>
          <el-input :model-value="panoramaLink" readonly size="small" style="margin-bottom:6px" />
          <el-button size="small" type="success" @click="copyPanoramaLink">复制链接</el-button>
          <el-button size="small" @click="window.open(panoramaLink, '_blank')">新窗口打开</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Location, Connection, Crop, Minus, Link,
  Aim, Edit, RefreshLeft, Delete, FullScreen, MoreFilled, Select,
  CircleCheck, Picture, VideoCamera, Grid, Loading
} from '@element-plus/icons-vue'
import { getEnterpriseList, getProblemList } from '@/api'
import { createMap, preloadAMap } from '@/utils/amap'
import { ElMessage } from 'element-plus'

const router = useRouter()

// ==================== 地图实例 ====================
let map = null
let AMapModule = null
const mapLoading = ref(true)
const DEFAULT_CENTER = [112.55, 37.87] // 太原市中心
const DEFAULT_ZOOM = 12

// ==================== 响应式状态 ====================
const keyword = ref('')
const coordInput = ref('')
const baseMap = ref('normal')
const activeTool = ref('')
const mouseCoord = ref('')

// 图层列表
const layerList = reactive([
  { key: 'pollution', label: '污染源点位', visible: true, color: '#F56C6C', count: 0, hovered: false },
  { key: 'sensitive', label: '周围敏感点', visible: true, color: '#E6A23C', count: 0, hovered: false },
  { key: 'device', label: '感知设备', visible: false, color: '#409EFF', count: 0, hovered: false },
  { key: 'personnel', label: '人员位置', visible: false, color: '#67C23A', count: 0, hovered: false }
])

// 图例
const legendList = reactive([
  { label: '违法企业', color: '#F56C6C' },
  { label: '正常企业', color: '#409EFF' },
  { label: '敏感点', color: '#E6A23C' },
  { label: '感知设备', color: '#909399' },
  { label: '网格员', color: '#67C23A' }
])

// 统计卡片
const mapStats = computed(() => ({
  enterprises: enterpriseMarkers.value.length,
  sensitivePoints: MOCK_SENSITIVE_POINTS.length,
  devices: MOCK_DEVICES.length,
  personnel: personnelMarkers.value.length
}))

const totalVisibleLayers = computed(() => layerList.filter(l => l.visible).length)

// 缓冲区圆圈
let bufferCircle = null
let nearbyCenterMarker = null

// 各类标记数组
const enterpriseMarkers = ref([])
const sensitiveMarkers = ref([])
const deviceMarkers = ref([])
const personnelMarkers = ref([])
const drawingLayers = ref([])  // 用户绘制的内容

// 周边查询
const nearbyVisible = ref(false)
const nearbyType = ref('enterprise')
const nearbyRadius = ref(1000)
const nearbyCenter = ref(null)
const nearbyResults = ref([])

// 缓冲区查询
const bufferVisible = ref(false)
const bufferStep = ref('pick') // pick | set
const bufferCenter = ref(null)
const bufferRadius = ref(500)
const bufferQueryType = ref('all')
const bufferResults = ref([])
let bufferCircleMap = null
let bufferCenterMarkerMap = null

// 条件查询企业
const advQueryVisible = ref(false)
const advQuerySearched = ref(false)
const advQueryForm = reactive({
  name: '',
  pollutionType: '',
  superviseType: '',
  address: '',
  violating: null
})
const advQueryResults = ref([])

// 全景图
const panoramaVisible = ref(false)
const panoramaPoint = ref(null)
const panoramaLink = ref('')
let panoramaPickActive = false

// 高级搜索
const showAdvanced = ref(false)
const advancedTab = ref('placename')
const placeKeyword = ref('')
const placeCity = ref('太原')
const placeType = ref('')
const placeResults = ref([])

const districtKeyword = ref('')
const districtList = ref([])
// 区划绘制缓存
let districtPolygons = []

const layerQueryType = ref('pollution')
const layerQueryKeyword = ref('')
const layerQueryResults = ref([])
const layerQuerySearched = ref(false)

// 业务库关联查询
const bizLinkType = ref('problem')
const bizLinkKeyword = ref('')
const bizLinkResults = ref([])
const bizLinkSearched = ref(false)

// 人员GPS定位
const geoEnabled = ref(false)
const geoCoord = ref('')
let geoWatchId = null

// 企业数据缓存
let allEnterprises = []
let allProblems = []

// ==================== 底图切换 ====================
function switchBaseMap(val) {
  if (!map) return
  // 清除卫星图层
  if (window._satelliteLayer) {
    window._satelliteLayer.setMap(null)
    window._satelliteLayer = null
  }
  switch (val) {
    case 'normal':
      map.setPitch(0)
      if (map.setMapStyle) map.setMapStyle('amap://styles/normal')
      break
    case 'satellite':
      map.setPitch(0)
      try {
        window._satelliteLayer = new AMapModule.TileLayer.Satellite()
        window._satelliteLayer.setMap(map)
      } catch (e) {
        console.warn('卫星图层加载失败:', e)
        ElMessage.warning('卫星图层暂不可用')
      }
      break
    case 'view3d':
      if (map.setMapStyle) map.setMapStyle('amap://styles/normal')
      map.setPitch(60)
      try {
        if (AMapModule.Buildings) {
          const buildings = new AMapModule.Buildings({ zooms: [14, 20], opacity: 1 })
          buildings.setMap(map)
        }
      } catch (e) { /* 建筑3D可选 */ }
      break
  }
}

// ==================== 搜索功能 ====================
function doSearch() {
  const kw = keyword.value?.trim()
  if (!kw) return
  if (!AMapModule) return

  const placeSearch = new AMapModule.PlaceSearch({
    city: '太原',
    pageSize: 10
  })

  placeSearch.search(kw, (status, result) => {
    if (status === 'complete' && result?.poiList?.pois?.length) {
      const pois = result.poiList.pois
      // 跳到第一个结果
      const poi = pois[0]
      map.setZoomAndCenter(16, [poi.location.lng, poi.location.lat])
      const infoWindow = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${poi.name}</strong><br/>${poi.address || ''}</div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      infoWindow.open(map, [poi.location.lng, poi.location.lat])

      // 高亮匹配的企业
      highlightMatchedEnterprises(kw)
    } else {
      // 本地搜索企业
      highlightMatchedEnterprises(kw)
    }
  })
}

function highlightMatchedEnterprises(kw) {
  const matches = allEnterprises.filter(e =>
    e.enterpriseName?.includes(kw) || e.address?.includes(kw)
  )
  if (matches.length > 0) {
    // 清除旧高亮
    enterpriseMarkers.value.forEach(m => m.setMap(map))
    // 高亮匹配项
    const matchedIds = new Set(matches.map(e => e.id))
    enterpriseMarkers.value.forEach(m => {
      if (matchedIds.has(m.getExtData()?.id)) {
        m.setAnimation('AMAP_ANIMATION_BOUNCE')
      }
    })
    if (matches.length === 1) {
      const e = matches[0]
      map.setZoomAndCenter(15, [e.longitude, e.latitude])
    }
    ElMessage.success(`找到 ${matches.length} 家匹配企业`)
  } else {
    ElMessage.info('未找到匹配企业')
  }
}

function clearSearch() {
  enterpriseMarkers.value.forEach(m => m.setAnimation(null))
}

// ==================== 坐标反解 ====================
async function doGeocode() {
  const input = coordInput.value?.trim()
  if (!input) return
  const parts = input.split(/[,，\s]+/)
  if (parts.length < 2) {
    ElMessage.warning('请输入格式: 经度,纬度')
    return
  }
  const lng = parseFloat(parts[0])
  const lat = parseFloat(parts[1])
  if (isNaN(lng) || isNaN(lat)) {
    ElMessage.warning('坐标格式不正确')
    return
  }
  if (!AMapModule) return

  const geocoder = new AMapModule.Geocoder()
  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result?.regeocode) {
      map.setZoomAndCenter(16, [lng, lat])
      const marker = new AMapModule.Marker({
        position: [lng, lat],
        title: result.regeocode.formattedAddress || '未知地点'
      })
      map.add(marker)
      drawingLayers.value.push(marker)
      const infoWindow = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>坐标反解结果</strong><br/>${result.regeocode.formattedAddress || '未知'}</div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      infoWindow.open(map, [lng, lat])
      ElMessage.success(result.regeocode.formattedAddress || '反解成功')
    } else {
      ElMessage.error('坐标反解失败')
    }
  })
}

// ==================== 图层切换 ====================
function toggleLayer(key) {
  const layer = layerList.find(l => l.key === key)
  if (!layer) return
  const vis = layer.visible
  switch (key) {
    case 'pollution':
      enterpriseMarkers.value.forEach(m => vis ? m.setMap(map) : m.setMap(null))
      break
    case 'sensitive':
      sensitiveMarkers.value.forEach(m => vis ? m.setMap(map) : m.setMap(null))
      break
    case 'device':
      deviceMarkers.value.forEach(m => vis ? m.setMap(map) : m.setMap(null))
      break
    case 'personnel':
      personnelMarkers.value.forEach(m => vis ? m.setMap(map) : m.setMap(null))
      break
  }
  updateLayerCounts()
}

// 全开/全关图层
function toggleAllLayers(visible) {
  layerList.forEach(l => {
    l.visible = visible
    toggleLayer(l.key)
  })
}

// 更新图层数量统计
function updateLayerCounts() {
  const polLayer = layerList.find(l => l.key === 'pollution')
  const senLayer = layerList.find(l => l.key === 'sensitive')
  const devLayer = layerList.find(l => l.key === 'device')
  const perLayer = layerList.find(l => l.key === 'personnel')
  if (polLayer) polLayer.count = enterpriseMarkers.value.length
  if (senLayer) senLayer.count = sensitiveMarkers.value.length
  if (devLayer) devLayer.count = deviceMarkers.value.length
  if (perLayer) perLayer.count = personnelMarkers.value.length
}

// ==================== 图层切换 ====================
function clearActiveTool() {
  activeTool.value = ''
  if (window._mouseTool) {
    window._mouseTool.close(true)
    window._mouseTool = null
  }
  if (window._rangingTool) {
    window._rangingTool.turnOff()
    window._rangingTool = null
  }
}

function startRanging() {
  if (activeTool.value === 'ranging') { clearActiveTool(); return }
  clearActiveTool()
  activeTool.value = 'ranging'
  window._rangingTool = new AMapModule.RangingTool(map)
  window._rangingTool.turnOn()
}

function startAreaMeasure() {
  if (activeTool.value === 'area') { clearActiveTool(); return }
  clearActiveTool()
  activeTool.value = 'area'
  window._mouseTool = new AMapModule.MouseTool(map)
  window._mouseTool.polygon()
  window._mouseTool.on('draw', (e) => {
    drawingLayers.value.push(e.obj)
    const area = AMapModule.GeometryUtil.ringArea(e.obj.getPath())
    const info = area > 1000000
      ? `${(area / 1000000).toFixed(2)} km²`
      : `${Math.round(area)} m²`
    ElMessage.success(`面积: ${info}`)
    clearActiveTool()
  })
}

function startDraw(type) {
  if (activeTool.value === type) { clearActiveTool(); return }
  clearActiveTool()
  activeTool.value = type
  window._mouseTool = new AMapModule.MouseTool(map)
  switch (type) {
    case 'line':
      // 直线用 polyline + 双击结束
      window._mouseTool.polyline({
        strokeColor: '#F56C6C',
        strokeWeight: 3,
        strokeOpacity: 0.9
      })
      break
    case 'polyline':
      window._mouseTool.polyline({
        strokeColor: '#409EFF',
        strokeWeight: 3,
        strokeOpacity: 0.8
      })
      break
    case 'polygon':
      window._mouseTool.polygon({
        fillColor: '#409EFF',
        fillOpacity: 0.15,
        strokeColor: '#409EFF',
        strokeWeight: 2
      })
      break
    case 'marker':
      window._mouseTool.marker()
      break
  }
  window._mouseTool.on('draw', (e) => {
    drawingLayers.value.push(e.obj)
    clearActiveTool()
    if (type === 'line' || type === 'polyline') {
      const path = e.obj.getPath()
      const dist = AMapModule.GeometryUtil.distanceOfLine(path)
      ElMessage.success(`${type === 'line' ? '直线' : '折线'}总长: ${Math.round(dist)} 米`)
    } else if (type === 'polygon') {
      const area = AMapModule.GeometryUtil.ringArea(e.obj.getPath())
      const info = area > 1000000
        ? `${(area / 1000000).toFixed(2)} km²`
        : `${Math.round(area)} m²`
      ElMessage.success(`区域面积: ${info}`)
    } else if (type === 'marker') {
      ElMessage.success('已添加标注点')
    }
  })
}

function clearDrawings() {
  drawingLayers.value.forEach(l => l.setMap?.(null) || map.remove?.(l))
  drawingLayers.value = []
  clearActiveTool()
  ElMessage.success('已清除所有标注')
}

function restoreMap() {
  clearDrawings()
  map.setZoomAndCenter(DEFAULT_ZOOM, DEFAULT_CENTER)
  map.setPitch(0)
  switchBaseMap('normal')
  baseMap.value = 'normal'
  clearSearch()
  ElMessage.success('地图已还原')
}

function goFullScreen() {
  const el = document.getElementById('full-map')
  el?.requestFullscreen?.()
}

// ==================== 条件查询企业 ====================
function resetAdvQuery() {
  advQueryForm.name = ''
  advQueryForm.pollutionType = ''
  advQueryForm.superviseType = ''
  advQueryForm.address = ''
  advQueryForm.violating = null
  advQueryResults.value = []
  advQuerySearched.value = false
}

function doAdvQuery() {
  advQuerySearched.value = true
  const { name, pollutionType, superviseType, address, violating } = advQueryForm
  const violatingIds = new Set(allProblems.map(p => p.enterpriseId).filter(Boolean))

  advQueryResults.value = allEnterprises.filter(e => {
    if (name && !e.enterpriseName?.includes(name)) return false
    if (pollutionType && e.pollutionType !== pollutionType) return false
    if (superviseType && e.superviseType !== superviseType) return false
    if (address && !e.address?.includes(address)) return false
    if (violating !== null) {
      const isV = violatingIds.has(e.id)
      if (violating === true && !isV) return false
      if (violating === false && isV) return false
    }
    return true
  }).map(e => ({
    ...e,
    isViolating: violatingIds.has(e.id)
  }))

  if (advQueryResults.value.length === 0) {
    ElMessage.info('未找到匹配企业')
  } else {
    ElMessage.success(`查询到 ${advQueryResults.value.length} 家企业`)
  }
}

function locateToEnterprise(row) {
  if (row.longitude && row.latitude) {
    map.setZoomAndCenter(15, [row.longitude, row.latitude])
    advQueryVisible.value = false
  }
}

function openEnterpriseDetail(id) {
  router.push(`/enterprise/${id}`)
}

// ==================== 全景图 ====================
function startPickPanoramaPoint() {
  panoramaPickActive = true
  panoramaVisible.value = false
  ElMessage.info('请在地图上点击选取全景点位')
  map.setDefaultCursor('crosshair')
}

function handlePanoramaPick(e) {
  if (!panoramaPickActive) return
  panoramaPickActive = false
  map.setDefaultCursor('default')
  const lnglat = e.lnglat
  panoramaPoint.value = [lnglat.lng.toFixed(6), lnglat.lat.toFixed(6)]
  panoramaLink.value = ''
  panoramaVisible.value = true
  ElMessage.success('已选取全景点位: ' + panoramaPoint.value.join(', '))
}

function openTencentStreetView() {
  if (!panoramaPoint.value) return
  const [lng, lat] = panoramaPoint.value
  panoramaLink.value = `https://map.qq.com/#pano=10041022101211110832400&heading=0&pitch=0&zoom=1&lng=${lng}&lat=${lat}`
  window.open(panoramaLink.value, '_blank')
}

function openGaodePanorama() {
  if (!panoramaPoint.value) return
  const [lng, lat] = panoramaPoint.value
  panoramaLink.value = `https://ditu.amap.com/dir?lng=${lng}&lat=${lat}&name=全景点位`
  window.open(panoramaLink.value, '_blank')
}

function copyPanoramaLink() {
  if (!panoramaLink.value) return
  navigator.clipboard?.writeText(panoramaLink.value).then(() => {
    ElMessage.success('已复制全景图链接')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

// ==================== 业务库关联查询 ====================
function doBizLinkSearch() {
  bizLinkSearched.value = true
  bizLinkResults.value = []
  const kw = bizLinkKeyword.value?.trim()
  const type = bizLinkType.value
  const violatingIds = new Set(allProblems.map(p => p.enterpriseId).filter(Boolean))

  switch (type) {
    case 'grid':
      // 网格数据（从区划查询缓存中获取）
      if (districtList.value.length) {
        const filtered = districtList.value.filter(d => !kw || d.name.includes(kw))
        bizLinkResults.value = filtered.map(d => ({
          name: d.name, bizType: '网格',
          lng: d.center?.lng, lat: d.center?.lat,
          id: d.name, location: d.center ? `${d.center.lng.toFixed(4)},${d.center.lat.toFixed(4)}` : '-'
        }))
      } else {
        bizLinkResults.value = [{ name: '太原市网格全域', bizType: '网格', lng: 112.55, lat: 37.87, id: 'ty' }]
      }
      break
    case 'problem':
      // 有GPS坐标的环境问题
      const problems = allProblems.filter(p => p.longitude && p.latitude)
      const filteredProblems = kw ? problems.filter(p =>
        p.description?.includes(kw) || p.problemDesc?.includes(kw) || p.id?.toString().includes(kw)
      ) : problems
      bizLinkResults.value = filteredProblems.map(p => ({
        name: p.description || p.problemDesc || `问题#${p.id}`,
        bizType: '环境问题',
        lng: p.longitude, lat: p.latitude,
        id: p.id, location: `${p.longitude.toFixed(4)},${p.latitude.toFixed(4)}`
      }))
      break
    case 'task':
      // 关联企业的巡查任务
      const tasks = allProblems.filter(p => p.enterpriseId).map(p => {
        const ent = allEnterprises.find(e => e.id === p.enterpriseId)
        return ent ? { name: `巡查:${ent.enterpriseName}`, bizType: '巡查任务', lng: ent.longitude, lat: ent.latitude, id: ent.id } : null
      }).filter(Boolean)
      const filteredTasks = kw ? tasks.filter(t => t.name.includes(kw)) : tasks
      bizLinkResults.value = [...new Map(filteredTasks.map(t => [t.id, t])).values()]
      break
    case 'assessment':
      // 关联企业（有考评数据的）
      const all = allEnterprises.filter(e => e.longitude && e.latitude)
      const filtered = kw ? all.filter(e => e.enterpriseName?.includes(kw) || e.pollutionType?.includes(kw)) : all
      bizLinkResults.value = filtered.map(e => ({
        name: e.enterpriseName,
        bizType: '企业',
        lng: e.longitude, lat: e.latitude,
        id: e.id, location: `${e.longitude.toFixed(4)},${e.latitude.toFixed(4)}`
      }))
      break
  }

  if (bizLinkResults.value.length === 0) {
    ElMessage.info('未找到关联记录')
  } else {
    ElMessage.success(`业务库关联: 找到 ${bizLinkResults.value.length} 条记录`)
  }
}

function mapBizLink(row) {
  if (row.lng && row.lat) {
    map.setZoomAndCenter(15, [row.lng, row.lat])
    if (AMapModule) {
      const iw = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${row.name}</strong><br/>业务类型: ${row.bizType}</div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      iw.open(map, [row.lng, row.lat])
    }
  }
}

// ==================== 周边查询 ====================
function openNearbySearch(lngLat) {
  nearbyCenter.value = [lngLat.lng, lngLat.lat]
  nearbyVisible.value = true
  nearbyResults.value = []
}

function doNearbySearch() {
  if (!nearbyCenter.value || !AMapModule) return
  const [clng, clat] = nearbyCenter.value
  nearbyResults.value = []
  const radius = nearbyRadius.value

  switch (nearbyType.value) {
    case 'enterprise': {
      const results = allEnterprises.filter(e => {
        if (!e.longitude || !e.latitude) return false
        const dist = AMapModule.GeometryUtil.distance(
          [clng, clat], [e.longitude, e.latitude]
        )
        return dist <= radius
      }).map(e => ({
        name: e.enterpriseName,
        address: e.address,
        lng: e.longitude,
        lat: e.latitude,
        id: e.id,
        distance: Math.round(AMapModule.GeometryUtil.distance([clng, clat], [e.longitude, e.latitude]))
      })).sort((a, b) => a.distance - b.distance)
      nearbyResults.value = results
      break
    }
    case 'sensitive': {
      const sens = getSensitivePoints()
      const results = sens.filter(s => {
        const dist = AMapModule.GeometryUtil.distance([clng, clat], [s.lng, s.lat])
        return dist <= radius
      }).map(s => ({
        name: s.name, address: s.address, lng: s.lng, lat: s.lat,
        distance: Math.round(AMapModule.GeometryUtil.distance([clng, clat], [s.lng, s.lat]))
      })).sort((a, b) => a.distance - b.distance)
      nearbyResults.value = results
      break
    }
    case 'pollution': {
      const results = allEnterprises.filter(e => {
        if (!e.longitude || !e.latitude) return false
        const dist = AMapModule.GeometryUtil.distance([clng, clat], [e.longitude, e.latitude])
        return dist <= radius
      }).map(e => ({
        name: e.enterpriseName, address: e.address, lng: e.longitude, lat: e.latitude, id: e.id,
        distance: Math.round(AMapModule.GeometryUtil.distance([clng, clat], [e.longitude, e.latitude]))
      })).sort((a, b) => a.distance - b.distance)
      nearbyResults.value = results
      break
    }
  }
  if (nearbyResults.value.length === 0) {
    ElMessage.info(`${radius}米内未找到结果`)
  } else {
    ElMessage.success(`找到 ${nearbyResults.value.length} 个结果`)
  }
}

function locateTo(row) {
  if (row.lng && row.lat) {
    map.setZoomAndCenter(16, [row.lng, row.lat])
    nearbyVisible.value = false
  }
}

// ==================== 缓冲区查询 ====================
function startBufferQuery() {
  if (activeTool.value === 'buffer') { clearActiveTool(); clearBufferCircle(); return }
  clearActiveTool()
  activeTool.value = 'buffer'
  bufferVisible.value = true
  bufferStep.value = 'pick'
  bufferCenter.value = null
  bufferResults.value = []
  clearBufferCircle()
  ElMessage.info('请在地图上点击以确定缓冲区中心点')
}

function handleBufferClick(e) {
  if (activeTool.value !== 'buffer') return
  if (!AMapModule) return
  const lnglat = e.lnglat
  bufferCenter.value = [lnglat.lng, lnglat.lat]
  bufferStep.value = 'set'
  clearBufferCircle()

  // 绘制中心点标记
  bufferCenterMarkerMap = new AMapModule.Marker({
    position: [lnglat.lng, lnglat.lat],
    content: `<div style="width:12px;height:12px;border-radius:50%;background:#E6A23C;border:3px solid #fff;box-shadow:0 0 8px rgba(230,162,60,0.6);transform:translate(-50%,-50%);animation:pulse 1.5s infinite"></div>`,
    anchor: 'center',
    zIndex: 998
  })
  bufferCenterMarkerMap.setMap(map)

  // 自动绘制缓冲区圆
  drawBufferCircle()
  ElMessage.success('已选定缓冲区中心，可设置半径查询')
}

function drawBufferCircle() {
  if (!bufferCenter.value || !AMapModule) return
  clearBufferCircle()
  bufferCircleMap = new AMapModule.Circle({
    center: bufferCenter.value,
    radius: bufferRadius.value,
    fillColor: '#409EFF',
    fillOpacity: 0.08,
    strokeColor: '#409EFF',
    strokeWeight: 2,
    strokeOpacity: 0.4,
    strokeStyle: 'dashed'
  })
  bufferCircleMap.setMap(map)
}

function clearBufferCircle() {
  if (bufferCircleMap) { bufferCircleMap.setMap(null); bufferCircleMap = null }
  if (bufferCenterMarkerMap) { bufferCenterMarkerMap.setMap(null); bufferCenterMarkerMap = null }
}

function doBufferSearch() {
  if (!bufferCenter.value || !AMapModule) return
  bufferResults.value = []
  const [clng, clat] = bufferCenter.value
  const radius = bufferRadius.value

  const allResults = []
  const addResults = (items, type) => {
    items.forEach(item => {
      const dist = AMapModule.GeometryUtil.distance([clng, clat], [item.lng, item.lat])
      if (dist <= radius) {
        allResults.push({ ...item, type, distance: Math.round(dist) })
      }
    })
  }

  if (bufferQueryType.value === 'all' || bufferQueryType.value === 'enterprise' || bufferQueryType.value === 'pollution') {
    addResults(allEnterprises.filter(e => e.longitude && e.latitude).map(e => ({
      name: e.enterpriseName, lng: e.longitude, lat: e.latitude, id: e.id, address: e.address,
      _type: '企业'
    })), '企业')
  }
  if (bufferQueryType.value === 'all' || bufferQueryType.value === 'sensitive') {
    addResults(MOCK_SENSITIVE_POINTS.map(s => ({
      name: s.name, lng: s.lng, lat: s.lat, address: s.address, _type: s.type
    })), '敏感点')
  }

  bufferResults.value = allResults.sort((a, b) => a.distance - b.distance)
  if (bufferResults.value.length === 0) {
    ElMessage.info('缓冲区内未找到匹配结果')
  } else {
    ElMessage.success(`缓冲区 (${radius}m) 内找到 ${bufferResults.value.length} 条记录`)
    // 自动缩放到缓冲区可见范围
    try { map.setFitView([bufferCircleMap]) } catch (e) { /* ignore */ }
  }
}

function locateToBuffer(row) {
  if (row.lng && row.lat) {
    map.setZoomAndCenter(16, [row.lng, row.lat])
    bufferVisible.value = false
  }
}

function clearBuffer() {
  bufferCenter.value = null
  bufferStep.value = 'pick'
  bufferResults.value = []
  clearBufferCircle()
  clearActiveTool()
}

// 调整缓冲区半径时自动重绘
watch(bufferRadius, () => {
  if (bufferCenter.value && activeTool.value === 'buffer') drawBufferCircle()
})

// ==================== 框选放大 ====================
function startRectSelect() {
  if (activeTool.value === 'rect') { clearActiveTool(); return }
  clearActiveTool()
  activeTool.value = 'rect'
  window._mouseTool = new AMapModule.MouseTool(map)
  window._mouseTool.rectangle({
    fillColor: '#409EFF',
    fillOpacity: 0.1,
    strokeColor: '#409EFF',
    strokeWeight: 2,
    strokeOpacity: 0.8,
    strokeStyle: 'dashed'
  })
  window._mouseTool.on('draw', (e) => {
    drawingLayers.value.push(e.obj)
    // 获取矩形范围并自动缩放到该区域
    const bounds = e.obj.getBounds()
    if (bounds) map.setBounds(bounds)
    clearActiveTool()
    ElMessage.success('已缩放至框选区域')
  })
}

// ==================== 地名库查询 ====================
function doPlaceSearch() {
  const kw = placeKeyword.value?.trim()
  if (!kw) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  if (!AMapModule) return

  const opts = {
    citylimit: placeCity.value !== '全国',
    pageSize: 30
  }
  if (placeType.value) {
    opts.type = placeType.value
  }
  const city = placeCity.value !== '全国' ? placeCity.value : undefined

  const placeSearch = new AMapModule.PlaceSearch({ ...opts, city: city || '全国', citylimit: !!city })
  placeSearch.search(kw, (status, result) => {
    if (status === 'complete' && result?.poiList?.pois?.length) {
      placeResults.value = result.poiList.pois.map(p => ({
        name: p.name,
        address: p.address,
        type: p.type,
        lng: p.location.lng,
        lat: p.location.lat
      }))
      // 自动跳到第一个结果
      const first = result.poiList.pois[0]
      map.setZoomAndCenter(15, [first.location.lng, first.location.lat])
      const iw = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${first.name}</strong><br/>${first.address || ''}</div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      iw.open(map, [first.location.lng, first.location.lat])
      ElMessage.success(`地名库检索: 找到 ${placeResults.value.length} 条结果`)
    } else {
      ElMessage.info('地名库未找到匹配结果')
    }
  })
}

function clearPlaceResults() {
  placeResults.value = []
  placeKeyword.value = ''
}

function locatePlace(p) {
  if (p.lng && p.lat) {
    map.setZoomAndCenter(17, [p.lng, p.lat])
    const iw = new AMapModule.InfoWindow({
      content: `<div style="padding:6px"><strong>${p.name}</strong><br/>${p.address || ''}<br/>类型: ${p.type || '-'}</div>`,
      offset: new AMapModule.Pixel(0, -30)
    })
    iw.open(map, [p.lng, p.lat])
  }
}

// ==================== 区划查询 ====================
function searchDistrict() {
  const kw = districtKeyword.value?.trim() || '太原市'
  if (!AMapModule) return
  clearDistrict()

  const district = new AMapModule.DistrictSearch({
    level: 'district',
    subdistrict: 1,
    extensions: 'all'
  })
  district.search(kw, (status, result) => {
    if (status === 'complete' && result?.districtList?.length) {
      const list = result.districtList[0]
      // 展平区县级
      const items = []
      items.push({ name: list.name, center: list.center, level: '市' })
      if (list.districtList) {
        list.districtList.forEach(d => {
          items.push({ name: d.name, center: d.center, level: '区', boundaries: d.boundaries })
        })
      }
      districtList.value = items
      ElMessage.success(`区划查询: ${items.length} 个行政区`)
    } else {
      ElMessage.info('未找到匹配区划')
    }
  })
}

function showDistrictOnMap(dist) {
  if (!AMapModule) return
  map.setZoomAndCenter(dist.level === '市' ? 10 : 13, dist.center ? [dist.center.lng, dist.center.lat] : DEFAULT_CENTER)
  if (dist.boundaries) {
    dist.boundaries.forEach(boundary => {
      const polygon = new AMapModule.Polygon({
        path: boundary,
        fillColor: '#409EFF',
        fillOpacity: 0.08,
        strokeColor: '#409EFF',
        strokeWeight: 2,
        strokeOpacity: 0.6,
        strokeStyle: 'dashed'
      })
      polygon.setMap(map)
      districtPolygons.push(polygon)
    })
  }
  const iw = new AMapModule.InfoWindow({
    content: `<div style="padding:6px"><strong>${dist.name}</strong><br/>行政级别: ${dist.level === '市' ? '市级' : '区县级'}</div>`,
    offset: new AMapModule.Pixel(0, -30)
  })
  iw.open(map, dist.center ? [dist.center.lng, dist.center.lat] : DEFAULT_CENTER)
}

function clearDistrict() {
  districtPolygons.forEach(p => p.setMap(null))
  districtPolygons = []
  districtList.value = []
}

// ==================== 图层编号查询 ====================
function doLayerSearch() {
  const kw = layerQueryKeyword.value?.trim()
  layerQuerySearched.value = true
  layerQueryResults.value = []

  if (!kw) {
    // 如果没有关键词，显示全部该图层数据
    switch (layerQueryType.value) {
      case 'pollution':
        layerQueryResults.value = allEnterprises.map(e => ({
          name: e.enterpriseName, type: '企业', status: '-',
          lng: e.longitude, lat: e.latitude, id: e.id
        }))
        break
      case 'sensitive':
        layerQueryResults.value = MOCK_SENSITIVE_POINTS.map(s => ({
          name: s.name, type: s.type, status: '-',
          lng: s.lng, lat: s.lat
        }))
        break
      case 'device':
        layerQueryResults.value = MOCK_DEVICES.map(d => ({
          name: d.name, type: d.type, status: d.status,
          lng: d.lng, lat: d.lat
        }))
        break
      case 'personnel':
        layerQueryResults.value = personnelMarkers.value.map(m => {
          const pos = m.getPosition()
          const d = m.getExtData()
          return {
            name: d?.name || '网格员',
            type: '人员',
            status: '在线',
            lng: pos?.lng, lat: pos?.lat
          }
        })
        break
    }
    return
  }

  // 有关键词时过滤
  const kwLow = kw.toLowerCase()
  switch (layerQueryType.value) {
    case 'pollution':
      layerQueryResults.value = allEnterprises.filter(e =>
        e.enterpriseName?.includes(kw) || e.id?.toString().includes(kw)
      ).map(e => ({
        name: e.enterpriseName, type: '企业', status: '-',
        lng: e.longitude, lat: e.latitude, id: e.id
      }))
      break
    case 'sensitive':
      layerQueryResults.value = MOCK_SENSITIVE_POINTS.filter(s =>
        s.name.includes(kw) || s.type.includes(kw)
      ).map(s => ({
        name: s.name, type: s.type, status: '-',
        lng: s.lng, lat: s.lat
      }))
      break
    case 'device':
      layerQueryResults.value = MOCK_DEVICES.filter(d =>
        d.name.includes(kw) || d.type.includes(kw)
      ).map(d => ({
        name: d.name, type: d.type, status: d.status,
        lng: d.lng, lat: d.lat
      }))
      break
    case 'personnel':
      layerQueryResults.value = personnelMarkers.value.filter(m => {
        const d = m.getExtData()
        return d?.name?.includes(kw) || d?.gridName?.includes(kw)
      }).map(m => {
        const pos = m.getPosition()
        const d = m.getExtData()
        return {
          name: d?.name || '网格员',
          type: '人员',
          status: '在线',
          lng: pos?.lng, lat: pos?.lat
        }
      })
      break
  }
  ElMessage.success(`图层检索: 找到 ${layerQueryResults.value.length} 条`)
}

function locateToLayerResult(row) {
  if (row.lng && row.lat) {
    map.setZoomAndCenter(16, [row.lng, row.lat])
  }
}

// ==================== 人员实时GPS定位 ====================
function startGeoLocation() {
  if (!navigator.geolocation) {
    console.warn('[Map] 浏览器不支持Geolocation')
    return
  }
  geoEnabled.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const { longitude, latitude } = pos.coords
      geoCoord.value = `${longitude.toFixed(4)}, ${latitude.toFixed(4)}`
      // 在地图上标注当前位置
      if (map && AMapModule) {
        const marker = new AMapModule.Marker({
          position: [longitude, latitude],
          content: `<div style="width:20px;height:20px;border-radius:50%;background:#67C23A;border:3px solid #fff;box-shadow:0 0 8px rgba(103,194,58,0.6);transform:translate(-50%,-50%);animation:pulse 2s infinite"></div>`,
          anchor: 'center',
          zIndex: 999,
          extData: { type: 'gps-self' }
        })
        marker.setMap(map)
        drawingLayers.value.push(marker)
        const iw = new AMapModule.InfoWindow({
          content: `<div style="padding:6px"><strong>📍 我的位置</strong><br/>精度: ${pos.coords.accuracy.toFixed(1)}m</div>`,
          offset: new AMapModule.Pixel(0, -30)
        })
        iw.open(map, [longitude, latitude])
        map.setZoomAndCenter(15, [longitude, latitude])
      }
    },
    (err) => {
      console.warn('[Map] GPS定位失败:', err.message)
      ElMessage.warning('GPS定位失败，请检查浏览器定位权限')
    },
    { enableHighAccuracy: true, timeout: 10000 }
  )
  // 持续监听位置变化
  geoWatchId = navigator.geolocation.watchPosition(
    (pos) => {
      const { longitude, latitude } = pos.coords
      geoCoord.value = `${longitude.toFixed(4)}, ${latitude.toFixed(4)}`
    },
    () => {},
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }
  )
}

function stopGeoLocation() {
  if (geoWatchId != null) {
    navigator.geolocation.clearWatch(geoWatchId)
    geoWatchId = null
  }
  geoEnabled.value = false
  geoCoord.value = ''
}

// ==================== 加载图层数据 ====================
async function loadAllData() {
  try {
    const [entRes, probRes] = await Promise.all([
      getEnterpriseList({ pageNum: 1, pageSize: 500 }),
      getProblemList({ pageNum: 1, pageSize: 500 })
    ])
    const entData = entRes.data || {}
    allEnterprises = entData.records || entData.list || []
    const probData = probRes.data || {}
    allProblems = probData.records || probData.list || []

    // 先清理旧标记
    enterpriseMarkers.value.forEach(m => m.setMap(null))
    enterpriseMarkers.value = []
    sensitiveMarkers.value.forEach(m => m.setMap(null))
    sensitiveMarkers.value = []
    deviceMarkers.value.forEach(m => m.setMap(null))
    deviceMarkers.value = []

    loadEnterprises()
    loadSensitivePoints()
    loadDevices()
    updateLayerCounts()
  } catch (e) {
    console.error('电子地图: 加载数据失败', e)
  }
}

function loadEnterprises() {
  if (!allEnterprises.length || !AMapModule) return

  const violatingIds = new Set(allProblems.map(p => p.enterpriseId).filter(Boolean))

  allEnterprises.filter(e => e.longitude && e.latitude).forEach(e => {
    const isViolating = violatingIds.has(e.id)
    const color = isViolating ? '#F56C6C' : '#409EFF'
    const marker = new AMapModule.Marker({
      position: [e.longitude, e.latitude],
      title: e.enterpriseName || '',
      content: `<div style="width:14px;height:14px;border-radius:50%;background:${color};border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,0.4);transform:translate(-50%,-50%)"></div>`,
      anchor: 'center',
      offset: new AMapModule.Pixel(0, 0),
      extData: { ...e, isViolating }
    })

    marker.on('click', () => {
      map.setZoomAndCenter(15, [e.longitude, e.latitude])
      const tag = isViolating
        ? '<span style="color:#F56C6C;font-weight:bold">[违法企业]</span> '
        : ''
      const content = `
        <div style="padding:10px;min-width:220px">
          <strong>${tag}${e.enterpriseName || '未知企业'}</strong>
          <br/>地址: ${e.address || '-'}
          <br/>污染类型: ${e.pollutionType || '-'}
          <br/>监管等级: ${e.superviseType || '-'}
          ${isViolating ? '<br/><span style="color:#F56C6C">该企业存在待处理环境问题</span>' : ''}
          <br/>
          <a href="javascript:void(0)" onclick="window.__openEnterpriseDetail(${e.id})"
             style="color:#409EFF;text-decoration:underline">查看一企一档 &gt;</a>
        </div>`
      window.__infoWindow?.close()
      window.__infoWindow = new AMapModule.InfoWindow({
        content,
        offset: new AMapModule.Pixel(0, -30)
      })
      window.__infoWindow.open(map, [e.longitude, e.latitude])
    })

    // 右键菜单
    marker.on('rightclick', (ev) => {
      openNearbySearch({ lng: e.longitude, lat: e.latitude })
    })

    enterpriseMarkers.value.push(marker)
    if (layerList.find(l => l.key === 'pollution')?.visible) {
      marker.setMap(map)
    }
  })

  // 全局函数：打开企业详情页
  window.__openEnterpriseDetail = (id) => {
    router.push(`/enterprise/${id}`)
  }
}

// 敏感点模拟数据（学校、医院、居民区、商业区，覆盖8市）
const MOCK_SENSITIVE_POINTS = [
  // 太原
  { name: '太原市第一中学', address: '迎泽区新建路', lng: 112.56, lat: 37.86, type: '学校' },
  { name: '太原市中心医院', address: '杏花岭区解放路', lng: 112.57, lat: 37.89, type: '医院' },
  { name: '迎泽公园', address: '迎泽区迎泽大街', lng: 112.55, lat: 37.85, type: '公园' },
  { name: '山西大学', address: '小店区坞城路', lng: 112.59, lat: 37.80, type: '学校' },
  { name: '太原市人民医院', address: '杏花岭区府东街', lng: 112.56, lat: 37.88, type: '医院' },
  { name: '漪汾苑小区', address: '万柏林区漪汾街', lng: 112.52, lat: 37.88, type: '居民区' },
  { name: '文华苑小区', address: '小店区长治路', lng: 112.57, lat: 37.83, type: '居民区' },
  { name: '龙潭公园', address: '杏花岭区城坊街', lng: 112.56, lat: 37.88, type: '公园' },
  // 大同
  { name: '大同市第一中学', address: '平城区御河西路', lng: 113.30, lat: 40.08, type: '学校' },
  { name: '大同市第三医院', address: '平城区新建南路', lng: 113.29, lat: 40.07, type: '医院' },
  { name: '大同公园', address: '平城区大北街', lng: 113.27, lat: 40.06, type: '公园' },
  // 阳泉
  { name: '阳泉市第一人民医院', address: '城区南大街', lng: 113.57, lat: 37.86, type: '医院' },
  { name: '阳泉市第一中学', address: '城区泉中路', lng: 113.59, lat: 37.84, type: '学校' },
  // 长治
  { name: '长治市人民医院', address: '潞州区长兴北路', lng: 113.11, lat: 36.19, type: '医院' },
  { name: '太行公园', address: '潞州区长兴路', lng: 113.09, lat: 36.18, type: '公园' },
  // 晋中
  { name: '榆次一中', address: '榆次区迎宾街', lng: 112.74, lat: 37.69, type: '学校' },
  { name: '晋中市第一医院', address: '榆次区顺城街', lng: 112.73, lat: 37.67, type: '医院' },
  // 临汾
  { name: '临汾市第一中学', address: '尧都区解放路', lng: 111.51, lat: 36.09, type: '学校' },
  { name: '临汾市人民医院', address: '尧都区向阳路', lng: 111.53, lat: 36.10, type: '医院' },
  // 吕梁
  { name: '吕梁市人民医院', address: '离石区滨河北路', lng: 111.14, lat: 37.54, type: '医院' },
  { name: '吕梁学院', address: '离石区学院路', lng: 111.10, lat: 37.49, type: '学校' },
  { name: '离石北城小区', address: '离石区龙凤北街', lng: 111.13, lat: 37.56, type: '居民区' },
  // 晋城
  { name: '晋城市人民医院', address: '城区文昌东街', lng: 112.85, lat: 35.49, type: '医院' },
  { name: '晋城市第一中学', address: '城区凤台东街', lng: 112.84, lat: 35.48, type: '学校' },
  { name: '泽州公园', address: '城区凤台西街', lng: 112.83, lat: 35.50, type: '公园' }
]

function getSensitivePoints() {
  return MOCK_SENSITIVE_POINTS
}

function loadSensitivePoints() {
  if (!AMapModule) return
  sensitiveMarkers.value.forEach(m => m.setMap(null))
  sensitiveMarkers.value = []

  MOCK_SENSITIVE_POINTS.forEach(s => {
    const marker = new AMapModule.Marker({
      position: [s.lng, s.lat],
      title: s.name,
      content: `<div style="width:12px;height:12px;border-radius:50%;background:#E6A23C;border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,0.4);transform:translate(-50%,-50%)"></div>`,
      anchor: 'center',
      extData: s
    })
    marker.on('click', () => {
      const iw = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${s.name}</strong><br/>类型: ${s.type}<br/>地址: ${s.address}</div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      iw.open(map, [s.lng, s.lat])
    })
    sensitiveMarkers.value.push(marker)
    if (layerList.find(l => l.key === 'sensitive')?.visible) marker.setMap(map)
  })
}

// 感知设备模拟数据（覆盖8市、多种类型）
const MOCK_DEVICES = [
  // 太原设备
  { name: '尖草坪空气质量监测站', lng: 112.565, lat: 37.930, type: '空气质量', status: '在线' },
  { name: '万柏林扬尘在线监测站', lng: 112.515, lat: 37.860, type: '扬尘', status: '在线' },
  { name: '小店区水质监测点', lng: 112.565, lat: 37.775, type: '水质', status: '在线' },
  { name: '杏花岭噪声监测站', lng: 112.570, lat: 37.890, type: '噪声', status: '在线' },
  { name: '尖草坪烟气在线监测站', lng: 112.555, lat: 37.940, type: '废气', status: '离线' },
  // 大同设备
  { name: '平城区空气质量监测站', lng: 113.275, lat: 40.045, type: '空气质量', status: '在线' },
  { name: '云冈区扬尘监测站', lng: 113.110, lat: 39.970, type: '扬尘', status: '在线' },
  { name: '云冈区水质自动监测站', lng: 113.090, lat: 39.935, type: '水质', status: '在线' },
  // 阳泉设备
  { name: '阳泉矿区空气质量站', lng: 113.505, lat: 37.885, type: '空气质量', status: '在线' },
  { name: '城区噪声在线监测点', lng: 113.575, lat: 37.855, type: '噪声', status: '离线' },
  // 长治设备
  { name: '潞州区空气质量监测站', lng: 113.115, lat: 36.195, type: '空气质量', status: '在线' },
  { name: '上党区VOCs监测点', lng: 113.065, lat: 36.040, type: 'VOCs', status: '在线' },
  { name: '潞州区水质在线监测站', lng: 113.125, lat: 36.185, type: '水质', status: '在线' },
  // 晋中设备
  { name: '榆次区空气质量监测站', lng: 112.745, lat: 37.700, type: '空气质量', status: '在线' },
  { name: '太谷区水环境监测点', lng: 112.555, lat: 37.425, type: '水质', status: '在线' },
  // 临汾设备
  { name: '尧都区空气质量监测站', lng: 111.535, lat: 36.075, type: '空气质量', status: '在线' },
  { name: '侯马市辐射监测站', lng: 111.365, lat: 35.615, type: '辐射', status: '在线' },
  // 吕梁设备
  { name: '离石区空气质量监测站', lng: 111.125, lat: 37.525, type: '空气质量', status: '在线' },
  { name: '孝义市烟气在线监测点', lng: 111.785, lat: 37.130, type: '废气', status: '在线' },
  // 晋城设备
  { name: '晋城城区空气超级站', lng: 112.855, lat: 35.495, type: '空气质量', status: '在线' },
  { name: '高平市水环境监测站', lng: 112.935, lat: 35.805, type: '水质', status: '离线' }
]

function loadDevices() {
  if (!AMapModule) return
  deviceMarkers.value.forEach(m => m.setMap(null))
  deviceMarkers.value = []

  MOCK_DEVICES.forEach(d => {
    const color = d.status === '在线' ? '#409EFF' : '#909399'
    const marker = new AMapModule.Marker({
      position: [d.lng, d.lat],
      title: d.name,
      content: `<div style="width:10px;height:10px;background:${color};border:2px solid #fff;box-shadow:0 1px 3px rgba(0,0,0,0.3);transform:translate(-50%,-50%)rotate(45deg)"></div>`,
      anchor: 'center',
      offset: new AMapModule.Pixel(4, -4),
      extData: d
    })
    marker.on('click', () => {
      const iw = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${d.name}</strong><br/>类型: ${d.type}<br/>状态: <span style="color:${color}">${d.status}</span></div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      iw.open(map, [d.lng, d.lat])
    })
    deviceMarkers.value.push(marker)
    if (layerList.find(l => l.key === 'device')?.visible) marker.setMap(map)
  })
}

// ==================== 人员定位（模拟网格员） ====================
function loadPersonnel() {
  if (!AMapModule) return
  personnelMarkers.value.forEach(m => m.setMap(null))
  personnelMarkers.value = []

  // 模拟网格员，覆盖8市
  const workers = [
    { id: 1, name: '张爱国', lng: 112.58, lat: 37.93, gridName: '尖草坪区', phone: '138****1001', city: '太原' },
    { id: 2, name: '李为民', lng: 112.52, lat: 37.86, gridName: '万柏林区', phone: '139****5678', city: '太原' },
    { id: 3, name: '郭静',   lng: 112.56, lat: 37.79, gridName: '小店区',   phone: '137****9012', city: '太原' },
    { id: 4, name: '王守正', lng: 112.58, lat: 37.90, gridName: '杏花岭区', phone: '136****3456', city: '太原' },
    { id: 5, name: '赵志强', lng: 112.57, lat: 37.87, gridName: '迎泽区',   phone: '135****7890', city: '太原' },
    { id: 6, name: '张大龙', lng: 113.28, lat: 40.04, gridName: '平城区',   phone: '138****0201', city: '大同' },
    { id: 7, name: '刘云',   lng: 113.10, lat: 39.97, gridName: '云冈区',   phone: '138****0202', city: '大同' },
    { id: 8, name: '黄毅',   lng: 111.12, lat: 37.52, gridName: '离石区',   phone: '138****0701', city: '吕梁' },
    { id: 9, name: '江怡',   lng: 111.78, lat: 37.14, gridName: '孝义市',   phone: '138****0702', city: '吕梁' },
    { id: 10, name: '宋涛',  lng: 112.85, lat: 35.50, gridName: '晋城区',   phone: '138****0801', city: '晋城' },
    { id: 11, name: '高阳',  lng: 112.93, lat: 35.80, gridName: '高平市',   phone: '138****0802', city: '晋城' },
    { id: 12, name: '马晓燕',lng: 111.52, lat: 36.07, gridName: '尧都区',   phone: '138****0601', city: '临汾' }
  ]

  workers.forEach(w => {
    const marker = new AMapModule.Marker({
      position: [w.lng, w.lat],
      title: w.name,
      content: `<div style="width:16px;height:16px;border-radius:50%;background:#67C23A;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.4);transform:translate(-50%,-50%)"></div>`,
      anchor: 'center',
      offset: new AMapModule.Pixel(0, 0),
      extData: { ...w, markerId: `worker-${w.id}` }
    })
    marker.on('click', () => {
      const iw = new AMapModule.InfoWindow({
        content: `<div style="padding:6px"><strong>${w.name}</strong><br/>网格: ${w.gridName}<br/>电话: ${w.phone}<br/><span style="color:#67C23A">● 在线</span></div>`,
        offset: new AMapModule.Pixel(0, -30)
      })
      iw.open(map, [w.lng, w.lat])
    })
    personnelMarkers.value.push(marker)
    if (layerList.find(l => l.key === 'personnel')?.visible) marker.setMap(map)
  })

  // 模拟人员位置实时更新（每10秒微调）
  startPersonnelSimulation(workers)
}

let personnelTimer = null
function startPersonnelSimulation(workers) {
  if (personnelTimer) clearInterval(personnelTimer)
  personnelTimer = setInterval(() => {
    personnelMarkers.value.forEach((marker, i) => {
      if (i >= workers.length) return
      const pos = marker.getPosition()
      if (!pos) return
      // 微调位置（模拟移动）
      const newLng = pos.lng + (Math.random() - 0.5) * 0.002
      const newLat = pos.lat + (Math.random() - 0.5) * 0.002
      marker.setPosition([newLng, newLat])
    })
  }, 10000)
}

// ==================== 生命周期 ====================
onMounted(async () => {
  try {
    // 预加载：应用启动时在其他页面已调用一次 preloadAMap()，这里多调用一次确保 SDK 已加载
    preloadAMap()
    map = await createMap('full-map', { zoom: DEFAULT_ZOOM, center: DEFAULT_CENTER })
    mapLoading.value = false
    if (!map) return
    AMapModule = window.AMap

    // 基础控件
    map.addControl(new AMapModule.Scale({ position: 'LB' }))
    map.addControl(new AMapModule.ToolBar({ position: 'RT' }))

    // 鼠标坐标显示
    map.on('mousemove', (e) => {
      mouseCoord.value = `${e.lnglat.lng.toFixed(4)}, ${e.lnglat.lat.toFixed(4)}`
    })

    // 右键菜单（周边查询）
    map.on('rightclick', (e) => {
      // 缓冲区模式下右键也触发缓冲区
      if (activeTool.value === 'buffer') {
        handleBufferClick(e)
        return
      }
      openNearbySearch(e.lnglat)
    })

    // 点击事件（缓冲区拾取 / 全景图拾取）
    map.on('click', (e) => {
      if (activeTool.value === 'buffer' && bufferStep.value === 'pick') {
        handleBufferClick(e)
        return
      }
      if (panoramaPickActive) {
        handlePanoramaPick(e)
        return
      }
    })

    // 加载所有数据
    loadAllData()
    loadPersonnel()

    // 预加载太原市区划数据（供区划查询使用）
    try {
      const district = new AMapModule.DistrictSearch({
        level: 'district',
        subdistrict: 1,
        extensions: 'all'
      })
      district.search('太原市', (status, result) => {
        if (status === 'complete' && result?.districtList) {
          const root = result.districtList[0]
          const items = []
          items.push({ name: root.name, center: root.center, level: '市' })
          if (root.districtList) {
            root.districtList.forEach(d => {
              items.push({ name: d.name, center: d.center, level: '区', boundaries: d.boundaries })
            })
          }
          districtList.value = items
          console.log(`[Map] 行政区划数据加载完成: ${items.length} 个区域`)
        }
      })
    } catch (e) {
      console.warn('[Map] 区划数据预加载失败:', e)
    }

    // 尝试获取GPS定位（非强制，失败不影响功能）
    setTimeout(() => {
      try { startGeoLocation() } catch (e) { /* GPS可选 */ }
    }, 2000)
  } catch (e) {
    console.error('电子地图: 初始化失败', e)
  }
})

onBeforeUnmount(() => {
  clearActiveTool()
  if (personnelTimer) clearInterval(personnelTimer)
  stopGeoLocation()
  clearDistrict()
  clearBufferCircle()
  enterpriseMarkers.value.forEach(m => m.setMap(null))
  sensitiveMarkers.value.forEach(m => m.setMap(null))
  deviceMarkers.value.forEach(m => m.setMap(null))
  personnelMarkers.value.forEach(m => m.setMap(null))
  drawingLayers.value.forEach(l => l.setMap?.(null))
  delete window.__openEnterpriseDetail
  if (window.__infoWindow) { window.__infoWindow.close(); window.__infoWindow = null }
  if (map) map.destroy()
})
</script>

<style scoped>
.fullmap-wrapper {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 搜索栏 ===== */
.map-search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: linear-gradient(180deg, #fff 0%, #f9fafc 100%);
  border-radius: 10px 10px 0 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  gap: 16px;
  flex-shrink: 0;
  border-bottom: 1px solid #eef1f6;
}
.search-left, .search-right {
  display: flex;
  gap: 8px;
  align-items: center;
}
.search-input { width: 260px; }
.coord-input { width: 200px; }

/* ===== 主体布局 ===== */
.map-body {
  display: flex;
  flex: 1;
  min-height: 0;
  position: relative;
}

/* ===== 左侧面板（重新设计）===== */
.layer-panel {
  width: 180px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
  border-right: 1px solid #e8ecf1;
  padding: 12px 10px;
  overflow-y: auto;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.panel-icon {
  font-size: 18px;
  color: #409EFF;
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
}
.layer-count-badge {
  margin-left: auto;
  background: #ecf5ff;
  color: #409EFF;
  font-size: 12px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 10px;
}
.layer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
  cursor: pointer;
  margin-bottom: 2px;
}
.layer-item:hover {
  background: #f0f5ff;
}
.layer-item-active {
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f8ff 100%);
}
.layer-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.layer-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.layer-label {
  font-size: 13px;
  color: #303133;
}
.layer-item-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.layer-badge {
  font-size: 12px;
}
.layer-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 4px 0;
}
.panel-divider {
  height: 1px;
  background: #e8ecf1;
  margin: 6px 0;
}
.panel-subtitle {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  padding-left: 4px;
  margin-bottom: 4px;
}
.legend-list {
  padding: 2px 4px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  padding: 2px 0;
}
.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  padding: 2px 0;
}
.stat-item {
  text-align: center;
  background: #fff;
  border-radius: 6px;
  padding: 6px 4px;
  border: 1px solid #ebeef5;
}
.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  font-size: 11px;
  color: #909399;
}
.stat-blue { color: #409EFF; }
.stat-orange { color: #E6A23C; }
.stat-blue2 { color: #67C23A; }
.stat-green { color: #67C23A; }
.stat-red { color: #F56C6C; }

/* ===== 地图容器 ===== */
.map-container {
  flex: 1;
  min-width: 0;
  background: #e8e8e8;
  position: relative;
}
.map-loading-full {
  position: absolute; inset: 0; z-index: 100; display: flex;
  flex-direction: column; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.9); gap: 14px;
}
.loading-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.loading-text-full { font-size: 15px; color: #909399; letter-spacing: 1px; }

/* ===== 右侧工具条（重新设计）===== */
.tool-panel {
  width: 52px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #fff 0%, #fafbfc 100%);
  border-left: 1px solid #e8ecf1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 4px;
  gap: 3px;
  z-index: 1;
}
.tool-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 44px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 10px;
  color: #606266;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.tool-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(64,158,255,0.08) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.25s;
}
.tool-btn:hover::before {
  opacity: 1;
}
.tool-btn:hover {
  color: #409EFF;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64,158,255,0.15);
}
.tool-btn.active {
  background: linear-gradient(135deg, #409EFF, #3a8ee6);
  color: #fff;
  box-shadow: 0 3px 10px rgba(64,158,255,0.3);
  transform: translateY(-1px);
}
.tool-btn .el-icon {
  font-size: 18px;
}
.tool-divider {
  width: 34px;
  height: 1px;
  background: #e8ecf1;
  margin: 4px 0;
}

/* ===== 底部状态栏 ===== */
.map-status {
  height: 28px;
  line-height: 28px;
  padding: 0 16px;
  background: linear-gradient(180deg, #f5f7fa 0%, #edf1f5 100%);
  border-top: 1px solid #e8ecf1;
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

/* ===== 高级搜索面板 ===== */
.advanced-panel {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 6px 12px;
  flex-shrink: 0;
}
.advanced-panel :deep(.el-tabs__header) {
  margin-bottom: 4px;
}
.adv-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  padding: 4px 0;
}
.adv-results {
  padding: 6px 0 2px;
}
.adv-count {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
}
.adv-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
  max-height: 120px;
  overflow-y: auto;
}

/* ===== GPS脉冲动画 ===== */
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(103,194,58,0.6); }
  70% { box-shadow: 0 0 0 15px rgba(103,194,58,0); }
  100% { box-shadow: 0 0 0 0 rgba(103,194,58,0); }
}
</style>
