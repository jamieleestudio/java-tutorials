package com.third.li;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结构化输出接口：GET /ai/structured
 */
@RestController
public class StructuredOutputController {

    private final MovieExtractor extractor;

    public StructuredOutputController(MovieExtractor extractor) {
        this.extractor = extractor;
    }

    @GetMapping("/ai/structured")
    public MovieExtractor.MovieInfo structured(
            @RequestParam(defaultValue = "《肖申克的救赎》是 1994 年上映的美国剧情片，由弗兰克·德拉邦特执导，改编自斯蒂芬·金的中篇小说，豆瓣评分 9.7。") String text) {
        return extractor.extract(text);
    }
}
