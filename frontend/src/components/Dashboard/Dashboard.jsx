import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { useWebSocket } from '../../hooks/useWebSocket'
import Screener from '../Screener/Screener'
import Alerts from '../Alerts/Alerts'
import StockList from './StockList'
import TickerTape from './TickerTape'
import { TrendingUp, LogOut, BarChart2, Bell, Activity, Search, User } from 'lucide-react'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

function Dashboard() {
  const { user, logout } = useAuth()
  const { liveStocks, screenerUpdates } = useWebSocket()
  const [activeTab, setActiveTab] = useState('stocks')
  const [globalSearch, setGlobalSearch] = useState('')
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    toast.success('Logged out')
    navigate('/login')
  }

  const tabs = [
    { id: 'stocks', label: 'Live Market', icon: Activity },
    { id: 'screener', label: 'Screener', icon: BarChart2 },
    { id: 'alerts', label: 'Alerts', icon: Bell },
  ]

  // Check if we have received any live WebSocket data yet
  const isConnected = Object.keys(liveStocks).length > 0

  return (
    <div className="flex h-screen bg-[#0B0E14] overflow-hidden text-gray-300">
      
      {/* LEFT SIDEBAR */}
      <aside className="w-64 bg-[#0a0a0a] border-r border-gray-800 flex flex-col hidden md:flex">
        <div className="h-16 flex items-center px-6 border-b border-gray-800">
          <div className="flex items-center gap-3">
            <div className="bg-green-500 p-1.5 rounded-lg shadow-[0_0_15px_rgba(34,197,94,0.3)]">
              <TrendingUp className="text-white" size={18} />
            </div>
            <span className="text-white font-bold tracking-wide">StockScreener</span>
          </div>
        </div>

        <nav className="flex-1 px-4 py-6 space-y-2">
          {tabs.map((tab) => {
            const Icon = tab.icon
            const isActive = activeTab === tab.id
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                  isActive
                    ? 'bg-green-500/10 text-green-400 font-semibold'
                    : 'text-gray-400 hover:bg-gray-900 hover:text-white'
                }`}
              >
                <Icon size={18} />
                {tab.label}
              </button>
            )
          })}
        </nav>

        <div className="p-4 border-t border-gray-800">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 text-gray-400 hover:text-white hover:bg-gray-900 rounded-xl transition-all"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT */}
      <main className="flex-1 flex flex-col min-w-0">
        
        {/* TOP HEADER */}
        <header className="h-16 bg-[#0a0a0a] border-b border-gray-800 flex items-center justify-between px-6 shrink-0">
          
          {/* Global Search */}
          <div className="relative w-96 hidden sm:block">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
            <input
              type="text"
              placeholder="Search markets (e.g. BTCUSDT, AAPL)..."
              value={globalSearch}
              onChange={(e) => setGlobalSearch(e.target.value)}
              className="w-full bg-[#111827] border border-gray-800 text-white text-sm rounded-lg pl-10 pr-4 py-2 focus:outline-none focus:border-green-500/50 focus:ring-1 focus:ring-green-500/50 transition-all placeholder-gray-600"
            />
          </div>

          {/* Right side tools */}
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="relative flex h-2.5 w-2.5">
                {isConnected && (
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                )}
                <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${isConnected ? 'bg-green-500' : 'bg-red-500'}`}></span>
              </div>
              <span className="text-xs font-mono text-gray-500 uppercase tracking-wider">
                {isConnected ? 'Market Live' : 'Connecting...'}
              </span>
            </div>

            <div className="h-6 w-px bg-gray-800"></div>

            <div className="flex items-center gap-2 bg-[#111827] border border-gray-800 rounded-full pl-1 pr-3 py-1 cursor-pointer hover:bg-gray-800 transition-colors">
              <div className="bg-gray-800 rounded-full p-1.5">
                <User size={14} className="text-gray-400" />
              </div>
              <span className="text-sm text-gray-300 font-medium">{user?.fullName}</span>
            </div>
          </div>
        </header>

        {/* TICKER TAPE */}
        <TickerTape liveStocks={liveStocks} />

        {/* SCROLLABLE VIEW AREA */}
        <div className="flex-1 overflow-auto p-6 scroll-smooth">
          <div className="max-w-7xl mx-auto">
            {activeTab === 'stocks' && (
              <StockList liveStocks={liveStocks} globalSearch={globalSearch} />
            )}
            {activeTab === 'screener' && (
              <Screener screenerUpdates={screenerUpdates} />
            )}
            {activeTab === 'alerts' && (
              <Alerts />
            )}
          </div>
        </div>

      </main>
    </div>
  )
}

export default Dashboard