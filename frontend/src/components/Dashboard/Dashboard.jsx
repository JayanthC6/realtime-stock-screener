import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { useWebSocket } from '../../hooks/useWebSocket'
import Screener from '../Screener/Screener'
import Alerts from '../Alerts/Alerts'
import StockList from './StockList'
import { TrendingUp, LogOut, BarChart2, Bell, Activity } from 'lucide-react'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

function Dashboard() {
  const { user, logout } = useAuth()
  const { liveStocks, screenerUpdates } = useWebSocket()
  const [activeTab, setActiveTab] = useState('stocks')
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    toast.success('Logged out')
    navigate('/login')
  }

  const tabs = [
    { id: 'stocks', label: 'Live Stocks', icon: Activity },
    { id: 'screener', label: 'Screener', icon: BarChart2 },
    { id: 'alerts', label: 'Alerts', icon: Bell },
  ]

  return (
    <div className="min-h-screen bg-gray-950">

      {/* Navbar */}
      <nav className="bg-gray-900 border-b border-gray-800 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-green-500 p-1.5 rounded-lg">
              <TrendingUp className="text-white" size={20} />
            </div>
            <span className="text-white font-bold text-lg">StockScreener</span>
          </div>

          <div className="flex items-center gap-4">
            <span className="text-gray-400 text-sm">
              Welcome, <span className="text-white">{user?.fullName}</span>
            </span>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 text-gray-400 hover:text-white transition-colors text-sm"
            >
              <LogOut size={16} />
              Logout
            </button>
          </div>
        </div>
      </nav>

      {/* Tabs */}
      <div className="bg-gray-900 border-b border-gray-800">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex gap-1">
            {tabs.map((tab) => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-4 py-4 text-sm font-medium border-b-2 transition-colors ${
                    activeTab === tab.id
                      ? 'border-green-500 text-green-400'
                      : 'border-transparent text-gray-400 hover:text-white'
                  }`}
                >
                  <Icon size={16} />
                  {tab.label}
                </button>
              )
            })}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-6 py-6">
        {activeTab === 'stocks' && (
          <StockList liveStocks={liveStocks} />
        )}
        {activeTab === 'screener' && (
          <Screener screenerUpdates={screenerUpdates} />
        )}
        {activeTab === 'alerts' && (
          <Alerts />
        )}
      </div>
    </div>
  )
}

export default Dashboard