package com.jetprobe.core.samples;

import com.jetprobe.core.JTestPipeline;
import com.jetprobe.core.structure.PipelineBuilder;
import com.jetprobe.core.task.SSHConfig;
import com.jetprobe.core.task.builder.TaskBuilder;
import scala.runtime.BoxedUnit;

/**
 * @author Shad.
 */
/*public class SamplePipe extends JTestPipeline {

    SSHConfig config = new SSHConfig("host", "user", "password");

    @Override
    public PipelineBuilder tasks() {

        @Task(description = "Awesome description", retryOnFailure = 3)
        jssh("run command", config, (sshClient -> {

                    sshClient.run("some command");

                    sshClient.upload("/from/source","/to/destination");


                })
        );





        return pipe();

    }
}*/
