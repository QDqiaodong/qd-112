package com.home.tools.init;

import com.home.tools.entity.Category;
import com.home.tools.entity.MaintenanceItem;
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
    private final CacheService cacheService;

    public DataInitializer(CategoryRepository categoryRepository,
                           MaintenanceItemRepository maintenanceItemRepository,
                           CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.maintenanceItemRepository = maintenanceItemRepository;
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
        List<Category> categories = new ArrayList<>();

        categories.add(createCategory("手动工具", "hand_tools", null, 1, 1, "各类手动操作工具"));
        categories.add(createCategory("扳手", "wrench", 1L, 2, 1, null));
        categories.add(createCategory("螺丝刀", "screwdriver", 1L, 2, 2, null));
        categories.add(createCategory("锤子", "hammer", 1L, 2, 3, null));
        categories.add(createCategory("钳子", "pliers", 1L, 2, 4, null));

        categories.add(createCategory("电动工具", "power_tools", null, 1, 2, "各类电力驱动工具"));
        categories.add(createCategory("电钻", "drill", 5L, 2, 1, null));
        categories.add(createCategory("电锯", "saw", 5L, 2, 2, null));
        categories.add(createCategory("角磨机", "grinder", 5L, 2, 3, null));
        categories.add(createCategory("热风枪", "heat_gun", 5L, 2, 4, null));

        categories.add(createCategory("测量工具", "measuring_tools", null, 1, 3, "各类测量和检测工具"));
        categories.add(createCategory("卷尺", "tape_measure", 9L, 2, 1, null));
        categories.add(createCategory("水平仪", "level", 9L, 2, 2, null));
        categories.add(createCategory("万用表", "multimeter", 9L, 2, 3, null));

        categories.add(createCategory("安全防护", "safety_tools", null, 1, 4, "各类安全防护用品"));
        categories.add(createCategory("护目镜", "goggles", 13L, 2, 1, null));
        categories.add(createCategory("手套", "gloves", 13L, 2, 2, null));
        categories.add(createCategory("口罩", "mask", 13L, 2, 3, null));

        categories.add(createCategory("园艺工具", "garden_tools", null, 1, 5, "各类园艺作业工具"));
        categories.add(createCategory("修枝剪", "pruner", 17L, 2, 1, null));
        categories.add(createCategory("铲子", "shovel", 17L, 2, 2, null));
        categories.add(createCategory("浇水壶", "watering_can", 17L, 2, 3, null));

        categoryRepository.saveAll(categories);
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
}
