package algorithm;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NumberSort {

    @Setter
    @Getter
    static class Menu {
        private String id;
        private String parentId;
        private String name;
        private Integer serialNumber;

        public Menu(String id, String parentId, String name, Integer serialNumber) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.serialNumber = serialNumber;
        }

        @Override
        public String toString() {
            return "Menu{" +
                    "id='" + id + '\'' +
                    ", parentId='" + parentId + '\'' +
                    ", name='" + name + '\'' +
                    ", serialNumber=" + serialNumber +
                    '}';
        }
    }

    public static void main(String[] args) {
        List<Menu> menus = new ArrayList<>();
        menus.add(new Menu("41362f5f-bcba-11e8-98b0-00163e0278b2", "zhengGaiZiLiaoKuYiJiId", "相关政策",1));
        menus.add(new Menu("41783287-bcba-11e8-98b0-00163e0278b2", "zhengCeZiLiaoKuYiJiId", "国家政策",1));
        menus.add(new Menu("6afe2621-9ff9-4dcf-af5e-c14af5793c2c", "a635d68f-338c-49f9-8aec-b3bee6ea8d2d", "教师个人资料",1));
        menus.add(new Menu("XUEXIAOWENHUAKU", null, "学校文化库",1));
        menus.add(new Menu("ac371b82-bdb5-41ce-8741-973be4c6fc1d", "XUEXIAOWENHUAKU", "音乐文化库",1));
        menus.add(new Menu("41a3b305-bcba-11e8-98b0-00163e0278b2", "zhengCeZiLiaoKuYiJiId", "省政策",2));
        menus.add(new Menu("58e89703-7919-11e9-9079-00163e0278b2", "zhengGaiZiLiaoKuYiJiId", "方法与工具",2));
        menus.add(new Menu("71265b40-4cd9-4ce1-ab09-8ed5aaf95822", "XUEXIAOWENHUAKU", "体育健身",2));
        menus.add(new Menu("XUEXIAOZHIDUKU", null, "学校制度库",2));
        menus.add(new Menu("41b05c9f-bcba-11e8-98b0-00163e0278b2", "zhengCeZiLiaoKuYiJiId", "市政策",3));
        menus.add(new Menu("zhengGaiZiLiaoKuYiJiId", null, "诊改资料库",3));
        menus.add(new Menu("zhengCeZiLiaoKuYiJiId", null, "政策资料库",4));
        menus.add(new Menu("80d178fd-938c-48c3-b6f9-5514b7a7fffd", null, "测试库",5));
        menus.add(new Menu("a635d68f-338c-49f9-8aec-b3bee6ea8d2d", null, "学校资料库",6));
        //排序
//        menus.sort(Comparator.comparing(Menu::getSerialNumber));
        for (Menu menu : menus) {
            System.out.println( menu);
        }
        System.out.println( "____________________________________________________________");

        List<Menu> rootList = menus.stream().filter(item -> item.getParentId() == null)
                        .sorted(Comparator.comparing(Menu::getSerialNumber)).toList();

        List<Menu> resultList = new ArrayList<>();
        for (Menu item : rootList) {
            buildTreeSortList(item, menus, resultList);
        }

        for(Menu menu : resultList){
            System.out.println(menu);
        }

    }



    private static void buildTreeSortList(Menu maintainVO,
                                   List<Menu> maintainVOList,
                                   List<Menu> resultList) {
        resultList.add(maintainVO);
        List<Menu> tempList = maintainVOList.stream()
                .filter(item -> maintainVO.getId().equals(item.getParentId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(tempList)){
            tempList = tempList.stream().sorted(Comparator.comparing(Menu::getSerialNumber)).collect(Collectors.toList());
            for (Menu ypSchoolKnowledgeBaseMaintainVO : tempList) {
                buildTreeSortList(ypSchoolKnowledgeBaseMaintainVO, maintainVOList, resultList);
            }
        }
    }
}
