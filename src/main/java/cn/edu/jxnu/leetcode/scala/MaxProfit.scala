package cn.edu.jxnu.leetcode.scala

/**
 * 给定一个数组，它的第 i 个元素是一支给定股票第 i 天的价格。
 * 如果你最多只允许完成一笔交易（即买入和卖出一支股票），设计一个算法来计算你所能获取的最大利润。
 * 注意你不能在买入股票前卖出股票。
 * @author 梦境迷离
 * @time 2018年8月14日
 * @version v1.0
 */
object MaxProfit extends App {

  print(maxProfit(Array(7, 1, 5, 3, 6, 4)))

  def maxProfit(prices: Array[Int]): Int = {

    if (prices.length == 0) return 0
    var max = 0
    var temp = prices(0)
    for (i <- 1 until prices.length) {
      if (temp > prices(i)) {
        temp = prices(i)
      } else {
        max = math.max(max, prices(i) - temp)
      }
    }
    max
  }
}