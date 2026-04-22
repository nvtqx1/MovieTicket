import React from 'react';

const SocialButton = ({ icon: Icon, provider, onClick }) => (
    <button
        onClick={onClick}
        className="flex items-center justify-center gap-2 w-full py-2.5 bg-[#262626] hover:bg-[#333]
               text-gray-300 text-sm font-medium rounded-md transition-colors border border-transparent hover:border-gray-700"
    >
        <img src={Icon} alt={provider} className="w-4 h-4" />
        <span>{provider}</span>
    </button>
);

export default SocialButton;