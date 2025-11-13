package test.revoke;

import java.util.HashMap;
import java.util.Map;

public class DataHandler {

    public static final Map<String,String> PROCESS_INSTANCE_MAP = new HashMap<>();
    public static final Map<String,String> ID_CARD_MAP = new HashMap<>();

    static {
        PROCESS_INSTANCE_MAP.put("王盛研",  "977e1afe-f009-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("杨延利",  "c71692ff-efd2-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("张长宇",  "0e3798f5-efce-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("王鹤洋",  "7a9775b2-efcb-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("王晓斌",  "86706460-ef62-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("张俊豪",  "2408c1f3-edae-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("宋文轩",  "e41b3f8b-edad-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("郭兆轩",  "b126a96d-edad-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("赵永帅",  "7157ab5e-edad-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("张良本",  "bd6825de-eda5-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("于清印",  "7d83ac7d-ed72-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("芦广顺",  "7cf084da-ed5e-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("刘国翔",  "42ed233c-ed5e-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("王长奥",  "d3417735-ecb6-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("刘彦霖",  "74b22972-ecb6-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("林志颖",  "f2eba8c5-ecb5-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("吴泽航",  "b8be50ab-ecb5-11ee-af0b-425f5bb9e332");
        PROCESS_INSTANCE_MAP.put("何洁",    "560f68c2-ecb5-11ee-af0b-425f5bb9e332");
    }

    static {
        ID_CARD_MAP.put("于清印",  "371581200707233876");
        ID_CARD_MAP.put("何洁",   "371525200805167269");
        ID_CARD_MAP.put("刘国翔",  "130433200801232915");
        ID_CARD_MAP.put("刘彦霖",  "371581200709300518");
        ID_CARD_MAP.put("吴泽航",  "220422200701180815");
        ID_CARD_MAP.put("宋文轩",  "371526200511023215");
        ID_CARD_MAP.put("张俊豪",  "371521200606186154");
        ID_CARD_MAP.put("张良本",  "371521200812255237");
        ID_CARD_MAP.put("张长宇",  "371581200711295738");
        ID_CARD_MAP.put("杨延利",  "371523200802172019");
        ID_CARD_MAP.put("林志颖",  "371525200702165017");
        ID_CARD_MAP.put("王晓斌",  "371522200709194233");
        ID_CARD_MAP.put("王盛研",  "370983200508161850");
        ID_CARD_MAP.put("王长奥",  "371522200802072716");
        ID_CARD_MAP.put("王鹤洋",  "371522200704245732");
        ID_CARD_MAP.put("芦广顺",  "371524200705100212");
        ID_CARD_MAP.put("赵永帅",  "130535200602204119");
        ID_CARD_MAP.put("郭兆轩",  "371522200510181611");
    }

    public static final String MONGO_QL_FORMAT = "db.bpe_biz_00012.update({\"sys_proc_inst_id\" : \"%s\"},{$set:{\"input_1676364327100\":{value: \"%s\"}}});";


    public static void check(){
        if(PROCESS_INSTANCE_MAP.size() !=18 ){
            throw new RuntimeException("error...");
        }
        if(PROCESS_INSTANCE_MAP.size() != ID_CARD_MAP.size()){
            throw new RuntimeException("error...");
        }
    }

    public static  void main(String args[]){
        check();
        PROCESS_INSTANCE_MAP.forEach((name,process)->{
            System.out.println(String.format(MONGO_QL_FORMAT,process,ID_CARD_MAP.get(name)));
        });
    }

}
