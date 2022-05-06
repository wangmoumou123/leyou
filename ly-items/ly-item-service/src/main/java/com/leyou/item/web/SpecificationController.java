package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据cid查询 group
     *
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));

    }

    @PostMapping("group")
    public ResponseEntity<Void> addSpec(@RequestBody SpecGroup specGroup) {
        specificationService.addSpec(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PutMapping("group")
    public ResponseEntity<Void> editSpec(@RequestBody SpecGroup specGroup) {
        specificationService.editSpec(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpec(@PathVariable("id") Long id) {
        specificationService.deleteSpec(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamsByGid(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false)Boolean searching) {
        return ResponseEntity.ok(specificationService.querySpecParamsByGid(gid, cid, searching));
    }

    @PostMapping("param")
    public ResponseEntity<Void> saveSpecParam(@RequestBody SpecParam specParam) {

        specificationService.saveSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> delSpecParam(@PathVariable("id") Long id) {
        specificationService.delSpecParam(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("param")
    public ResponseEntity<Void> editSpecParam(@RequestBody SpecParam specParam) {
        specificationService.editSpecParam(specParam);
        return ResponseEntity.ok().build();
    }

    /**
     * restful风格
     *
     * @param cid
     * @return
     */
    @GetMapping("params/{cid}")
    public ResponseEntity<List<SpecParam>> querySpecParamByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.status(HttpStatus.OK).body(specificationService.querySpecParamByCid(cid));
    }

    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryGroupByCidSpec(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListByCid(cid));

    }

}
