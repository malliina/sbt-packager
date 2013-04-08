package com.mle.sbt.azure

import sbt._
import AzureKeys._
import java.nio.file.{Path, Paths}
import com.mle.util.Implicits._
import com.mle.util.Util
import com.mle.azure.StorageClient

/**
 *
 * @author mle
 */
trait AzurePlugin extends Plugin {
  val azureSettings: Seq[Setting[_]] = Seq(
    azurePackage := None,
    azureConf := (Paths get sys.props("user.home")) / "keys" / "azure-storage.sec",
    azureContainer <<= (azureConf, azureContainerName) map ((conf, cont) => {
      val (account, key) = readCredentials(conf)
      val client = new StorageClient(account, key)
      client container cont
    })
  )

  private def readCredentials(file: Path) = {
    val credMap = Util.props(file.toAbsolutePath.toString)
    val accountName = credMap("account_name")
    val accountKey = credMap("account_key")
    (accountName, accountKey)
  }
}

object AzurePlugin extends AzurePlugin