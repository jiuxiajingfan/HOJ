package top.hcode.hoj.judger;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2021/1/22 21:13
 * @Description:
 */
@Slf4j
public class JudgeC extends JudgeStrategy {

    private String code;

    private Problem problem;

    private Long submitId;

    public JudgeC(Problem problem, Judge judge) {
        super(problem,judge.getSubmitId());
        this.submitId = judge.getSubmitId();
        this.code = judge.getCode();
        this.problem = problem;
    }


    @Override
    public HashMap<String, Object> judge() {
        // 该提交对应的文件夹路径
        String fileDir = Constants.Compiler.WORKPLACE.getContent() + "/" + submitId;
        // 将代码写入文件
        String srcFilePath = fileDir + "/" + Constants.CompileConfig.C.getSrcName();
        FileWriter fileWriter = new FileWriter(srcFilePath, CharsetUtil.UTF_8);
        fileWriter.write(code);
        // 编译指令
        String command = Constants.CompileConfig.C.getCommand();
        // 输出exe文件路径
        String exePath = fileDir+"/"+Constants.CompileConfig.C.getExeName();

        String spjCompileResult = checkOrCompileSpj(problem.getSpjCode(), problem.getSpjLanguage());

        // 如果该题是需要特别判题的，但是该特别判题程序不存在或编译时产生异常，则此次判题直接判系统错误。
        if (!spjCompileResult.equals("success")){
            HashMap<String,Object> result = new HashMap<>();
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("msg", spjCompileResult);
            return result;
        }

        // 编译的一些时间空间参数等
        Long maxCpuTime = Constants.CompileConfig.C.getMaxCpuTime();
        Long maxRealTime = Constants.CompileConfig.C.getMaxRealTime();
        Long maxMemory = Constants.CompileConfig.C.getMaxMemory();
        List<String> envs = Constants.RunConfig.C.getEnvs();

        // 运行的一些指令参数，环境配置等
        String runCommand = Constants.RunConfig.C.getCommand();
        List<String> runEnvs = Constants.RunConfig.C.getEnvs();
        String runSeccompRule = Constants.RunConfig.C.getSeccompRule();
        Integer memoryLimitCheckOnly = Constants.RunConfig.C.getMemoryLimitCheckOnly();

        // 特别判题的参数
        String spjRunSeccompRule = null;
        if (problem.getSpjLanguage().equals("C")) {
            spjRunSeccompRule = Constants.RunConfig.SPJ_C.getSeccompRule();
        }else if(problem.getSpjLanguage().equals("C++")){
            spjRunSeccompRule = Constants.RunConfig.SPJ_CPP.getSeccompRule();
        }

        // 调用安全沙盒进行编译操作
        HashMap<String, Object> result = compile(srcFilePath, fileDir, command, exePath, maxCpuTime,maxRealTime,maxMemory,envs);
        if (!(Boolean) result.get("result")){ // 编译失败
            // {msg:error,code:STATUS_COMPILE_ERROR}
            result.remove("result");
            return result;
        }else{
            // 编译成功，则进行每个测试点进行测试
            List<JSONObject> testCaseResultList = judgeAllCase(exePath, fileDir, (long) problem.getTimeLimit(), problem.getTimeLimit() * 3L,
                    problem.getMemoryLimit()*1024*1024L, runCommand, runEnvs, runSeccompRule, spjRunSeccompRule, memoryLimitCheckOnly);
            // 获取判题结果
            return getJudgeInfo(testCaseResultList, problem.getType() == 0);
        }
    }
}