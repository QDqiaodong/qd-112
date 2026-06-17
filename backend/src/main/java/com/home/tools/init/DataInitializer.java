package com.home.tools.init;

import com.home.tools.entity.Category;
import com.home.tools.entity.CategoryMaintenanceItem;
import com.home.tools.entity.MaintenanceItem;
import com.home.tools.repository.CategoryMaintenanceItemRepository;
import com.home.tools.repository.CategoryRepository;
import com.home.tools.repository.MaintenanceItemRepository;
import com.home.tools.service.CacheService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final CategoryMaintenanceItemRepository categoryMaintenanceItemRepository;
    private final CacheService cacheService;

    public DataInitializer(CategoryRepository categoryRepository,
                           MaintenanceItemRepository maintenanceItemRepository,
                           CategoryMaintenanceItemRepository categoryMaintenanceItemRepository,
                           CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
        this.categoryMaintenanceItemRepository = categoryMaintenanceItemRepository;
        this.cacheService = cacheService;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            initCategories();
        }
        if (maintenanceItemRepository.count() == 0) {
            initMaintenanceItems();
        }
        if (categoryMaintenanceItemRepository.count() == 0) {
            initCategoryMaintenanceItems();
        }
        cacheService.evictCategoryCache();
        cacheService.cacheCategoryTree(categoryRepository.findAll());

        Map<String, String> cycles = new HashMap<>();
        List<MaintenanceItem> items = maintenanceItemRepository.findAll();
        for (MaintenanceItem item : items) {
            cycles.put(item.getCode(), String.valueOf(item.getDefaultCycleDays()));
        }
        cacheService.cacheMaintenanceCycles(cycles);
    }

    private void initCategories() {
        Category handTools = createCategory("手动工具", "hand_tools", null, 1, 1, "各类手动操作工具");
        Category powerTools = createCategory("电动工具", "power_tools", null, 1, 2, "各类电力驱动工具");
        Category measuringTools = createCategory("测量工具", "measuring_tools", null, 1, 3, "各类测量和检测工具");
        Category safetyTools = createCategory("安全防护", "safety_tools", null, 1, 4, "各类安全防护用品");
        Category gardenTools = createCategory("园艺工具", "garden_tools", null, 1, 5, "各类园艺作业工具");

        categoryRepository.saveAll(List.of(handTools, powerTools, measuringTools, safetyTools, gardenTools));

        List<Category> children = new ArrayList<>();
        children.add(createCategory("扳手", "wrench", handTools.getId(), 2, 1, null));
        children.add(createCategory("螺丝刀", "screwdriver", handTools.getId(), 2, 2, null));
        children.add(createCategory("锤子", "hammer", handTools.getId(), 2, 3, null));
        children.add(createCategory("钳子", "pliers", handTools.getId(), 2, 4, null));

        children.add(createCategory("电钻", "drill", powerTools.getId(), 2, 1, null));
        children.add(createCategory("电锯", "saw", powerTools.getId(), 2, 2, null));
        children.add(createCategory("角磨机", "grinder", powerTools.getId(), 2, 3, null));
        children.add(createCategory("热风枪", "heat_gun", powerTools.getId(), 2, 4, null));

        children.add(createCategory("卷尺", "tape_measure", measuringTools.getId(), 2, 1, null));
        children.add(createCategory("水平仪", "level", measuringTools.getId(), 2, 2, null));
        children.add(createCategory("万用表", "multimeter", measuringTools.getId(), 2, 3, null));

        children.add(createCategory("护目镜", "goggles", safetyTools.getId(), 2, 1, null));
        children.add(createCategory("手套", "gloves", safetyTools.getId(), 2, 2, null));
        children.add(createCategory("口罩", "mask", safetyTools.getId(), 2, 3, null));

        children.add(createCategory("修枝剪", "pruner", gardenTools.getId(), 2, 1, null));
        children.add(createCategory("铲子", "shovel", gardenTools.getId(), 2, 2, null));
        children.add(createCategory("浇水壶", "watering_can", gardenTools.getId(), 2, 3, null));

        categoryRepository.saveAll(children);
    }

    private void initMaintenanceItems() {
        List<MaintenanceItem> items = new ArrayList<>();
        items.add(createMaintenanceItem("清洁除尘", "clean", "清除工具表面灰尘和碎屑", 30));
        items.add(createMaintenanceItem("润滑上油", "oil", "对活动部件进行润滑", 60));
        items.add(createMaintenanceItem("螺丝紧固", "tighten", "检查并紧固松动的螺丝", 90));
        items.add(createMaintenanceItem("零件检查", "inspect", "检查各零部件磨损情况", 180));
        items.add(createMaintenanceItem("电池维护", "battery", "电动工具电池充放电维护", 30));
        items.add(createMaintenanceItem("电线检查", "cable", "检查电线绝缘和接头", 90));
        items.add(createMaintenanceItem("刀具打磨", "sharpen", "打磨切割工具刀刃", 60));
        items.add(createMaintenanceItem("防锈处理", "rustproof", "对金属部件进行防锈处理", 180));
        maintenanceItemRepository.saveAll(items);
    }

    private void initCategoryMaintenanceItems() {
        Map<String, Long> catMap = new HashMap<>();
        for (Category cat : categoryRepository.findAll()) {
            catMap.put(cat.getCode(), cat.getId());
        }

        List<CategoryMaintenanceItem> items = new ArrayList<>();

        Long handToolsId = catMap.get("hand_tools");
        if (handToolsId != null) {
            items.add(createCatItem(handToolsId, "clean", 25, true, "手动工具易沾染灰尘"));
            items.add(createCatItem(handToolsId, "oil", 45, true, null));
            items.add(createCatItem(handToolsId, "tighten", null, true, null));
            items.add(createCatItem(handToolsId, "inspect", 150, true, null));
            items.add(createCatItem(handToolsId, "rustproof", 120, true, "手动工具金属部件需定期防锈"));
        }

        Long powerToolsId = catMap.get("power_tools");
        if (powerToolsId != null) {
            items.add(createCatItem(powerToolsId, "clean", 20, true, "电动工具防尘要求高"));
            items.add(createCatItem(powerToolsId, "oil", null, true, null));
            items.add(createCatItem(powerToolsId, "tighten", null, true, null));
            items.add(createCatItem(powerToolsId, "inspect", 90, true, "电动工具零部件检查周期较短"));
            items.add(createCatItem(powerToolsId, "battery", 20, true, "电池需定期充放电维护"));
            items.add(createCatItem(powerToolsId, "cable", 60, true, "电线安全检查"));
            items.add(createCatItem(powerToolsId, "rustproof", null, true, null));
        }

        Long measuringToolsId = catMap.get("measuring_tools");
        if (measuringToolsId != null) {
            items.add(createCatItem(measuringToolsId, "clean", 15, true, "测量工具清洁要求高"));
            items.add(createCatItem(measuringToolsId, "inspect", 60, true, "精度部件定期检查"));
            items.add(createCatItem(measuringToolsId, "rustproof", null, true, null));
        }

        Long gardenToolsId = catMap.get("garden_tools");
        if (gardenToolsId != null) {
            items.add(createCatItem(gardenToolsId, "clean", 20, true, "园艺工具泥土清理"));
            items.add(createCatItem(gardenToolsId, "sharpen", 45, true, "园艺工具需经常打磨"));
            items.add(createCatItem(gardenToolsId, "rustproof", 90, true, "接触潮湿土壤需防锈"));
            items.add(createCatItem(gardenToolsId, "oil", null, true, null));
        }

        Long drillId = catMap.get("drill");
        if (drillId != null) {
            items.add(createCatItem(drillId, "battery", 15, true, "电钻电池使用频繁需加强维护"));
            items.add(createCatItem(drillId, "inspect", 60, true, "电钻核心部件检查"));
        }

        Long wrenchId = catMap.get("wrench");
        if (wrenchId != null) {
            items.add(createCatItem(wrenchId, "rustproof", 60, true, "扳手金属接触面防锈"));
        }

        categoryMaintenanceItemRepository.saveAll(items);
    }

    private Category createCategory(String name, String code, Long parentId, Integer level, Integer sortOrder, String description) {
        Category cat = new Category();
        cat.setName(name);
        cat.setCode(code);
        cat.setParentId(parentId);
        cat.setLevel(level);
        cat.setSortOrder(sortOrder);
        cat.setDescription(description);
        return cat;
    }

    private MaintenanceItem createMaintenanceItem(String name, String code, String description, Integer defaultCycleDays) {
        MaintenanceItem item = new MaintenanceItem();
        item.setName(name);
        item.setCode(code);
        item.setDescription(description);
        item.setDefaultCycleDays(defaultCycleDays);
        return item;
    }

    private CategoryMaintenanceItem createCatItem(Long categoryId, String code, Integer customCycleDays, Boolean enabled, String remarks) {
        CategoryMaintenanceItem item = new CategoryMaintenanceItem();
        item.setCategoryId(categoryId);
        item.setMaintenanceItemCode(code);
        item.setCustomCycleDays(customCycleDays);
        item.setEnabled(enabled);
        item.setRemarks(remarks);
        return item;
    }
}
