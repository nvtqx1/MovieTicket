import React from 'react';

const StatCard = ({ title, value, icon, color }) => (
    <div className="bg-[#111] border border-white/5 p-5 rounded-xl flex items-center justify-between shadow-lg">
        <div>
            <p className="text-[10px] text-gray-500 uppercase tracking-widest font-bold mb-1">{title}</p>
            <h3 className="text-2xl font-black text-white">{value}</h3>
        </div>
        <div className={`p-3 rounded-lg bg-opacity-10 ${color}`}>
            <span className="text-xl">{icon}</span>
        </div>
    </div>
);

export default StatCard;