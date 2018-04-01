import collection._
import JavaConversions._
import org.apache.mahout.math._
import scalabindings._
import RLikeOps._
import drm._
import org.apache.mahout.sparkbindings._



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

object ScalaTest {

  def main(args: Array[String]) {
    implicit val mahoutCtx = mahoutSparkContext(
      masterUrl = "local",
      appName = "MahoutLocalContext"
    )
    val inCoreA = dense((1, 2, 3), (2, 3, 4), (3, 4, 5))
    val inCoreB = dense((3, 4, 5), (5, 6, 7))
    
    val drmA = drmParallelize(m = inCoreA, numPartitions = 3)
    val drmB = drmParallelize(m = inCoreB, numPartitions = 2)
    println("Hello, world!") // prints Hello World
  }
}
