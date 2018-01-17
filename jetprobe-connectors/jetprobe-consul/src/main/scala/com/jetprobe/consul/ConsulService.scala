package com.jetprobe.consul

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.storage.{Storage, StorageQuery}
import com.jetprobe.core.validations.ValidationRule

/**
  * @author Shad.
  */
case class ConsulService(host : Expr = Expr("localhost"),
                         port : Expr = Expr("8500")) extends Storage {

}

