import { useEffect, useState } from 'react'
import { getAllStocks } from '../../services/api'
import { TrendingUp, TrendingDown } from 'lucide-react'

function StockList({ liveStocks }) {
  const [stocks, setStocks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchStocks()
    const interval = setInterval(fetchStocks, 10000)
    return () => clearInterval(interval)
  }, [])

  // Merge live WebSocket updates with fetched stocks
  useEffect(() => {
    if (Object.keys(liveStocks).length > 0) {
      setStocks(prev => prev.map(stock => {
        const live = liveStocks[stock.symbol]
        return live ? { ...stock, ...live } : stock
      }))
    }
  }, [liveStocks])

  const fetchStocks = async () => {
    try {
      const response = await getAllStocks()
      setStocks(response.data)
    } catch (error) {
      console.error('Error fetching stocks:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-400">Loading stocks...</div>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-white text-lg font-semibold">
          Live Stock Prices
        </h2>
        <span className="text-gray-500 text-sm">
          {stocks.length} stocks tracked
        </span>
      </div>

      {stocks.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <Activity className="text-gray-600 mx-auto mb-3" size={40} />
          <p className="text-gray-400">
            Waiting for live data from Finnhub...
          </p>
          <p className="text-gray-600 text-sm mt-1">
            Make sure your backend is running and Finnhub API key is set
          </p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800">
                <th className="text-left text-gray-400 text-xs font-medium px-4 py-3">SYMBOL</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">PRICE</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">CHANGE</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">VOLUME</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">RSI</th>
                <th className="text-right text-gray-400 text-xs font-medium px-4 py-3">P/E</th>
              </tr>
            </thead>
            <tbody>
              {stocks.map((stock, index) => {
                const isPositive = stock.priceChange >= 0
                return (
                  <tr
                    key={stock.symbol}
                    className={`border-b border-gray-800/50 hover:bg-gray-800/30 transition-colors ${
                      liveStocks[stock.symbol] ? 'bg-green-950/20' : ''
                    }`}
                  >
                    <td className="px-4 py-3">
                      <span className="text-white font-semibold text-sm">
                        {stock.symbol}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className="text-white text-sm">
                        ${stock.currentPrice?.toFixed(2) ?? '--'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className={`flex items-center justify-end gap-1 text-sm ${
                        isPositive ? 'text-green-400' : 'text-red-400'
                      }`}>
                        {isPositive
                          ? <TrendingUp size={14} />
                          : <TrendingDown size={14} />
                        }
                        {stock.priceChangePercent?.toFixed(2) ?? '--'}%
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className="text-gray-400 text-sm">
                        {stock.volume?.toLocaleString() ?? '--'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className={`text-sm font-medium ${
                        stock.rsi > 70
                          ? 'text-red-400'
                          : stock.rsi < 30
                          ? 'text-green-400'
                          : 'text-gray-300'
                      }`}>
                        {stock.rsi?.toFixed(1) ?? '--'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <span className="text-gray-400 text-sm">
                        {stock.peRatio?.toFixed(1) ?? '--'}
                      </span>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default StockList